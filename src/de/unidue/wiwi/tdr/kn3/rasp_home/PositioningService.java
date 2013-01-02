package de.unidue.wiwi.tdr.kn3.rasp_home;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class PositioningService extends IntentService implements Observer {

	private static final int NOTIFICATION_ID = 1;

	private NotificationCompat.Builder mBuilder;
	NotificationManager mNotificationManager;

	private WiFiClass wifi;
	private PositioningClass positions;

	public PositioningService() {
		super("PositioningService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(MainApplication.RH_TAG, "Start PositioningService");
		positions = PositioningClass.loadPositions(this, "pref_positioning_calibrate");
		wifi = new WiFiClass(this);
		wifi.observer.addObserver(this);
		wifi.StartScan(Integer.parseInt(MainApplication.pref.getString("pref_positioning_interval", "6000")));

		mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(getString(R.string.notification_positioning_title))
				.setContentText(getString(R.string.notification_positioning_text));
		Intent resultIntent = new Intent(this, SettingsActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(SettingsActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
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
	public void update(Observable observable, Object data) {
		if (data instanceof List) {
			@SuppressWarnings("unchecked")
			List<ScanResult> results = (List<ScanResult>) data;
			String location = positions.getBestLocation(new PositioningClass.Position(results));
			Log.d(MainApplication.RH_TAG, "Best found location: " + location);
			mBuilder.setContentText(location.equals("") ? getString(R.string.notification_positioning_text) : location);
			mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
			// TODO Send result to server
		}
	}
}
