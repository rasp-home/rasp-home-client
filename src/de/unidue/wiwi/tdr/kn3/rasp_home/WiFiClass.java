package de.unidue.wiwi.tdr.kn3.rasp_home;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

public class WiFiClass extends BroadcastReceiver {

	public static final int MIN_INTERVAL = 4000;
	public Observable<List<ScanResult>> observer;

	private WifiManager wifi;
	private Context context;

	private boolean scan = false;
	private int interval;

	public WiFiClass(Context context) {
		this.context = context;
		wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		observer = new Observable<List<ScanResult>>();
	}

	public boolean StartScan(int interval) {
		if (!scan && interval >= MIN_INTERVAL) {
			this.interval = interval;

			if (!wifi.isWifiEnabled()) {
				Toast.makeText(context, context.getString(R.string.error_no_wlan), Toast.LENGTH_LONG).show();
			}

			scan = true;
			context.registerReceiver(this, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
			wifi.startScan();
			return true;
		} else {
			return false;
		}
	}

	public boolean StopScan() {
		if (scan) {
			context.unregisterReceiver(this);
			scan = false;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		if (scan) {
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
