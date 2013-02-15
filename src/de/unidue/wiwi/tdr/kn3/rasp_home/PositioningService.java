package de.unidue.wiwi.tdr.kn3.rasp_home;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

public class PositioningService extends IntentService implements Observer<String> {

	private static final int NOTIFICATION_ID = 1;

	private String lastLocation;

	private NotificationCompat.Builder mBuilder;
	private NotificationManager mNotificationManager;

	public PositioningService() {
		super("PositioningService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(MainApplication.RH_TAG, "Start PositioningService");
		MainApplication.pos.observer.addObserver(this);
		MainApplication.pos.StartScan(MainApplication.pref.getInt("pref_positioning_interval",
				PositioningClass.MIN_INTERVAL));

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
		MainApplication.pos.observer.deleteObserver(this);
		MainApplication.pos.StopScan();
		stopForeground(true);
	}

	@Override
	public void update(Observable<String> o, String arg) {
		mBuilder.setContentText(lastLocation == null ? getString(R.string.notification_positioning_text)
				: lastLocation);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

		if (arg != null) {
			Log.d(MainApplication.RH_TAG, "Best found location: " + arg);
		} else {
			Toast.makeText(this, getString(R.string.error_no_position), Toast.LENGTH_SHORT).show();
		}
		// TODO Send result to server etc
	}

	public String GetLastLocation() {
		return lastLocation;
	}
}
