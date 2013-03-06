package de.unidue.wiwi.tdr.kn3.rasp_home;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

public class PositioningClass implements Serializable, Observer<List<ScanResult>> {

	private static final long serialVersionUID = -6557936159576437331L;

	public static final int MIN_INTERVAL = WiFi.MIN_INTERVAL;

	public Observable<String> observer;

	public String lastLocation = null;

	private String recordLocation = null;

	private WiFi wifi;
	private Map<String, List<Position>> locations;
	private Map<String, Position> meanLocations;

	public PositioningClass(Context context) {
		observer = new Observable<String>();
		wifi = new WiFi(context);
		locations = new HashMap<String, List<Position>>();
		meanLocations = new HashMap<String, Position>();
	}

	public void updateLocations(String[] updateLocations) {
		for (String location : updateLocations) {
			if (!meanLocations.containsKey(location)) {
				locations.put(location, new ArrayList<Position>());
				meanLocations.put(location, new Position());
			}
		}
		for (String location : meanLocations.keySet()) {
			boolean contains = false;
			for (String updateLocation : updateLocations) {
				if (location.equals(updateLocation)) {
					contains = true;
					break;
				}
			}
			if (!contains) {
				locations.remove(location);
				meanLocations.remove(location);
			}
		}
	}

	public Set<String> getLocations() {
		return meanLocations.keySet();
	}

	public int getPositionsCount(String location) {
		if (meanLocations.containsKey(location)) {
			return meanLocations.get(location).getUpdates();
		} else {
			return 0;
		}
	}

	public boolean removePositions(String location) {
		if (meanLocations.containsKey(location)) {
			locations.remove(location);
			meanLocations.remove(location);
			return true;
		} else {
			return false;
		}
	}

	public boolean StartRecord(String location) {
		recordLocation = location;
		return StartScan(WiFi.MIN_INTERVAL);
	}

	public boolean StopRecord() {
		recordLocation = null;
		return StopScan();
	}

	public boolean StartScan(int interval) {
		wifi.observer.addObserver(this);
		return wifi.StartScan(interval);
	}

	public boolean StopScan() {
		wifi.observer.deleteObserver(this);
		return wifi.StopScan();
	}

	private boolean addPositionScanResults(String location, List<ScanResult> results) {
		if (meanLocations.containsKey(location) && results.size() >= 3) {
			locations.get(location).add(new Position(results));
			meanLocations.get(location).updatePosition(results);
			return true;
		} else {
			return false;
		}
	}

	private String getBestLocation(Position comparePosition) {
		String location = null;
		double distance = 0, bestDistance = Double.MAX_VALUE;

		for (Entry<String, Position> meanPosition : meanLocations.entrySet()) {
			distance = meanPosition.getValue().getDistance(comparePosition);
			if (distance < bestDistance) {
				location = meanPosition.getKey();
				bestDistance = distance;
			}
		}

		return location;
	}

	@Override
	public void update(Observable<List<ScanResult>> o, List<ScanResult> arg) {
		//TODO Update automatical new positions
		if (recordLocation != null) {
			if (addPositionScanResults(recordLocation, arg)) {
				observer.notifyObservers("");
			} else {
				observer.notifyObservers(null);
			}
		} else {
			lastLocation = getBestLocation(new PositioningClass.Position(arg));
			if (lastLocation != null) {
				addPositionScanResults(lastLocation, arg);
			}
			observer.notifyObservers(lastLocation);
		}
	}

	public static PositioningClass loadPositions(Context context, String fileName) {
		PositioningClass positions = null;
		if (context.getFileStreamPath(fileName).exists()) {
			try {
				FileInputStream fis = context.openFileInput(fileName);
				ObjectInputStream ois = new ObjectInputStream(fis);
				positions = (PositioningClass) ois.readObject();
				ois.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return positions == null ? new PositioningClass(context) : positions;
	}

	public static void savePositions(Context context, String fileName, PositioningClass positions) {
		try {
			FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(positions);
			oos.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static class Position implements Serializable {

		private static final long serialVersionUID = 5710190099743727158L;

		private Map<String, Integer> stations;
		private int updates;
		
		public Position() {
			stations = new HashMap<String, Integer>();
			updates = 0;
		}

		public Position(List<ScanResult> results) {
			this();
			updatePosition(results);
		}

		public int getUpdates() {
			return updates;
		}

		public double getDistance(Position position) {
			int count = 0;
			double distance = 0;
			for (Entry<String, Integer> station : stations.entrySet()) {
				if (position.stations.get(station.getKey()) != null) {
					distance += Math.pow(station.getValue() - position.stations.get(station.getKey()), 2);
					count++;
				}
			}
			if (count >= 3)
				return Math.sqrt(distance);
			else
				return Double.MAX_VALUE;
		}

		public void updatePosition(List<ScanResult> results) {
			for (ScanResult result : results) {
				if (stations.containsKey(result.BSSID)) {
					stations.put(result.BSSID, (stations.get(result.BSSID) + result.level) / 2);
				} else {
					stations.put(result.BSSID, result.level);
				}
			}
			updates++;
		}
	}

	private class WiFi extends BroadcastReceiver implements Serializable {

		private static final long serialVersionUID = 5710190099743727158L;

		public static final int MIN_INTERVAL = 4000;

		public Observable<List<ScanResult>> observer;

		private WifiManager wifi;
		private Context context;

		private boolean scanRun = false;
		private int interval;

		public WiFi(Context context) {
			this.context = context;
			observer = new Observable<List<ScanResult>>();
			wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		}

		public boolean StartScan(int interval) {
			if (!scanRun && interval >= MIN_INTERVAL) {
				this.interval = interval;

				if (!wifi.isWifiEnabled()) {
					Toast.makeText(context, context.getString(R.string.error_no_wifi), Toast.LENGTH_LONG).show();
				}

				context.registerReceiver(this, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
				wifi.startScan();
				scanRun = true;
				return true;
			} else {
				return false;
			}
		}

		public boolean StopScan() {
			if (scanRun) {
				context.unregisterReceiver(this);
				scanRun = false;
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if (scanRun) {
				Log.d(MainApplication.RH_TAG, "New scan results");
				observer.notifyObservers(wifi.getScanResults());
				try {
					Thread.sleep(interval - MIN_INTERVAL);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				wifi.startScan();
			}
		}
	}
}
