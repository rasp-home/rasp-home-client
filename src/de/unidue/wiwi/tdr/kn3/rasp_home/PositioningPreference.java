package de.unidue.wiwi.tdr.kn3.rasp_home;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

public class PositioningPreference extends DialogPreference implements Observer<List<ScanResult>> {

	private WiFiClass wifi;
	private PositioningClass positions;

	private ToggleButton record_togglebutton;
	private Spinner locations_spinner;
	private List<String> locations_list;
	private ArrayAdapter<String> locations_adapter;
	private TextView save_textview;
	private Button erase_button;

	public PositioningPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		getContext().stopService(MainApplication.positioningService);

		positions = PositioningClass.loadPositions(getContext(), getKey());
		positions.updateLocations(getContext().getResources()
				.getStringArray(R.array.dialog_positioning_locations_names));
		wifi = new WiFiClass(getContext());

		record_togglebutton = (ToggleButton) view.findViewById(R.id.dialog_positioning_record_toggleButton);
		record_togglebutton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					wifi.observer.addObserver(PositioningPreference.this);
					wifi.StartScan(WiFiClass.MIN_INTERVAL);
				} else {
					wifi.observer.deleteObserver(PositioningPreference.this);
					wifi.StopScan();
				}
			}
		});
		locations_spinner = (Spinner) view.findViewById(R.id.dialog_positioning_locations_spinner);
		locations_list = new ArrayList<String>();
		locations_list.addAll(positions.getLocations());
		locations_adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, locations_list);
		locations_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		locations_spinner.setAdapter(locations_adapter);
		locations_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				save_textview.setText(getContext().getResources().getQuantityString(
						R.plurals.dialog_positioning_save_count, positions.getPositionsCount(locations_list.get(arg2)),
						positions.getPositionsCount(locations_list.get(arg2))));
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		save_textview = (TextView) view.findViewById(R.id.dialog_positioning_save_textview);
		save_textview.setText(getContext().getResources().getQuantityString(R.plurals.dialog_positioning_save_count,
				positions.getPositionsCount(locations_list.get(locations_spinner.getSelectedItemPosition())),
				positions.getPositionsCount(locations_list.get(locations_spinner.getSelectedItemPosition()))));
		erase_button = (Button) view.findViewById(R.id.dialog_positioning_erase_button);
		erase_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				positions.removePositions(locations_list.get(locations_spinner.getSelectedItemPosition()));
				save_textview.setText(getContext().getResources().getQuantityString(
						R.plurals.dialog_positioning_save_count,
						positions.getPositionsCount(locations_list.get(locations_spinner.getSelectedItemPosition())),
						positions.getPositionsCount(locations_list.get(locations_spinner.getSelectedItemPosition()))));
			}
		});
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult) {
			PositioningClass.savePositions(getContext(), getKey(), positions);
		}

		wifi.observer.deleteObserver(this);
		wifi.StopScan();

		if (MainApplication.pref.getBoolean("pref_positioning_tracking", false)) {
			getContext().startService(MainApplication.positioningService);
		}
	}

	@Override
	public void update(Observable<List<ScanResult>> o, List<ScanResult> arg) {
		positions.addPositionScanResults(locations_list.get(locations_spinner.getSelectedItemPosition()), arg);
		save_textview.setText(getContext().getResources().getQuantityString(R.plurals.dialog_positioning_save_count,
				positions.getPositionsCount(locations_list.get(locations_spinner.getSelectedItemPosition())),
				positions.getPositionsCount(locations_list.get(locations_spinner.getSelectedItemPosition()))));
	}

}
