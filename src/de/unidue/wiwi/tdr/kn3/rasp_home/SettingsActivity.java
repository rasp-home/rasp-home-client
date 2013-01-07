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
			pref.setSummary(String.format(getString(R.string.pref_positioning_interval_summary), pref
					.getSharedPreferences().getInt(pref.getKey(), NumberPickerPreference.DEFAULT_VALUE)));
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
			if (arg1.equals("pref_positioning_tracking")) {
				if (arg0.getBoolean(arg1, false)) {
					getActivity().startService(MainApplication.wifiService);
				} else {
					getActivity().stopService(MainApplication.wifiService);
				}
			} else if (arg1.equals("pref_positioning_interval")) {
				Preference pref = findPreference(arg1);
				try {
					pref.setSummary(String.format(getString(R.string.pref_positioning_interval_summary),
							arg0.getInt(pref.getKey(), NumberPickerPreference.DEFAULT_VALUE)));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}

				if (MainApplication.pref.getBoolean("pref_positioning_tracking", false)) {
					getActivity().stopService(MainApplication.wifiService);
					getActivity().startService(MainApplication.wifiService);
				}
			}
		}

	}

}