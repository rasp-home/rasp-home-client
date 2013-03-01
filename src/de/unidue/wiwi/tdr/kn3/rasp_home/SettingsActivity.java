package de.unidue.wiwi.tdr.kn3.rasp_home;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
	}

	public static class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);

			Preference pref = findPreference("pref_positioning_interval");
			if (pref != null)
				pref.setSummary(String.format(getString(R.string.pref_positioning_interval_summary), pref
						.getSharedPreferences().getInt(pref.getKey(), 0)));
			pref = findPreference("pref_communication_user");
			if (pref != null)
				pref.setSummary(pref.getSharedPreferences().getString(pref.getKey(), ""));
			pref = findPreference("pref_communication_pass");
			if (pref != null)
				pref.setSummary(pref.getSharedPreferences().getString(pref.getKey(), ""));
			pref = findPreference("pref_communication_serverport");
			if (pref != null)
				pref.setSummary(String.format(getString(R.string.pref_communication_serverport_summary), pref
						.getSharedPreferences().getInt(pref.getKey(), 0)));
			pref = findPreference("pref_communication_zeroconfigport");
			if (pref != null)
				pref.setSummary(String.format(getString(R.string.pref_communication_zeroconfport_summary), pref
						.getSharedPreferences().getInt(pref.getKey(), 0)));
			pref = findPreference("pref_communication_timeout");
			if (pref != null)
				pref.setSummary(String.format(getString(R.string.pref_communication_timeout_summary), pref
						.getSharedPreferences().getInt(pref.getKey(), 0)));
		}

		@Override
		public void onResume() {
			super.onResume();
			getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onPause() {
			super.onPause();
			getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
			Preference pref = findPreference(arg1);
			if (arg1.equals("pref_positioning_tracking")) {
				if (arg0.getBoolean(arg1, false)) {
					getActivity().startService(MainApplication.positioningService);
				} else {
					getActivity().stopService(MainApplication.positioningService);
				}
			} else if (arg1.equals("pref_positioning_interval")) {
				pref.setSummary(String.format(getString(R.string.pref_positioning_interval_summary),
						arg0.getInt(arg1, 0)));
				if (MainApplication.pref.getBoolean("pref_positioning_tracking", false)) {
					getActivity().stopService(MainApplication.positioningService);
					getActivity().startService(MainApplication.positioningService);
				}
			} else if (arg1.equals("pref_communication_user")) {
				pref.setSummary(arg0.getString(arg1, ""));
				MainApplication.com.client.SetUserPass(arg0.getString("pref_communication_user", "") + ":" + arg0.getString("pref_communication_pass", ""));
			} else if (arg1.equals("pref_communication_pass")) {
				pref.setSummary(arg0.getString(arg1, ""));
				MainApplication.com.client.SetUserPass(arg0.getString("pref_communication_user", "") + ":" + arg0.getString("pref_communication_pass", ""));
			} else if (arg1.equals("pref_communication_serverport")) {
				pref.setSummary(String.format(getString(R.string.pref_communication_serverport_summary),
						arg0.getInt(arg1, 0)));
				MainApplication.com.server.Stop();
				MainApplication.com.server.Start(MainApplication.pref.getInt("pref_communication_serverport", 8888));
			} else if (arg1.equals("pref_communication_zeroconfigport")) {
				pref.setSummary(String.format(getString(R.string.pref_communication_zeroconfport_summary),
						arg0.getInt(arg1, 0)));
				MainApplication.com.zeroconf.Stop();
				MainApplication.com.zeroconf.Start(MainApplication.pref.getInt("pref_communication_zeroconfport", 1234));				
			} else if (arg1.equals("pref_communication_timeout")) {
				pref.setSummary(String.format(getString(R.string.pref_communication_timeout_summary),
						arg0.getInt(arg1, 0)));
				MainApplication.com.client.SetTimeout(MainApplication.pref.getInt("pref_communication_timeout", 5000));				
			}
		}

	}

}