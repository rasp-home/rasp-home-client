package de.unidue.wiwi.tdr.kn3.rasp_home;

import java.util.List;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class PositioningService extends IntentService implements Observer<List<ScanResult>> {

	public Observable<String> observer;

	private static final int NOTIFICATION_ID = 1;

	private String lastLocation;

	private NotificationCompat.Builder mBuilder;
	private NotificationManager mNotificationManager;

	private WiFiClass wifi;
	private PositioningClass positions;

	public PositioningService() {
		super("PositioningService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(MainApplication.RH_TAG, "Start PositioningService");
		observer = new Observable<String>();
		wifi = new WiFiClass(this);
		wifi.observer.addObserver(this);
		wifi.StartScan(MainApplication.pref.getInt("pref_positioning_interval", WiFiClass.MIN_INTERVAL));
		positions = PositioningClass.loadPositions(this, "pref_positioning_calibrate");
		
		// TODO Create Communication

		mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(getString(R.string.notification_positioning_title))
				.setContentText(getString(R.string.notification_positioning_text));
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(SettingsActivity.class);
		stackBuilder.addNextIntent(new Intent(this, SettingsActivity.class));
		mBuilder.setContentIntent(stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT));
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
		startForeground(NOTIFICATION_ID, mBuilder.build());
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		synchronized (this) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(MainApplication.RH_TAG, "Stop PositioningService");
		wifi.observer.deleteObserver(this);
		wifi.StopScan();
		stopForeground(true);
	}

	@Override
	public void update(Observable<List<ScanResult>> o, List<ScanResult> arg) {
		lastLocation = positions.getBestLocation(new PositioningClass.Position(arg));
		Log.d(MainApplication.RH_TAG, "Best found location: " + lastLocation);
		observer.notifyObservers(lastLocation);

		// TODO Send result to server

		mBuilder.setContentText(lastLocation.equals("") ? getString(R.string.notification_positioning_text)
				: lastLocation);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

	public String GetLastLocation() {
		return lastLocation;
	}
}
