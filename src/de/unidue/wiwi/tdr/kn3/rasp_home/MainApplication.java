package de.unidue.wiwi.tdr.kn3.rasp_home;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class MainApplication extends Application {

	public static final String RH_TAG = "RH";
	public static Intent positioningService;
	public static SharedPreferences pref;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(RH_TAG, "Start Application");
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		positioningService = new Intent(this, PositioningService.class);
		pref = PreferenceManager.getDefaultSharedPreferences(this);

		if (pref.getBoolean("pref_positioning_tracking", false)) {
			startService(positioningService);
		}
	}

}
