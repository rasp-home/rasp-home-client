package de.unidue.wiwi.tdr.kn3.rasp_home;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

public class NumberPickerPreference extends DialogPreference {

	public static final int DEFAULT_MINVALUE = Integer.MIN_VALUE;
	public static final int DEFAULT_MAXVALUE = Integer.MAX_VALUE;
	public static final int DEFAULT_VALUE = 0;

	NumberPicker picker;
	int currentValue;
	int minValue;
	int maxValue;

	public NumberPickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NumberPickerPreference);
		minValue = a.getInt(R.styleable.NumberPickerPreference_minValue, DEFAULT_MINVALUE);
		maxValue = a.getInt(R.styleable.NumberPickerPreference_maxValue, DEFAULT_MAXVALUE);
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);

		picker = (NumberPicker) view.findViewById(R.id.dialog_numberpicker_numberpicker);
		picker.setMinValue(minValue);
		picker.setMaxValue(maxValue);
		picker.setWrapSelectorWheel(false);
		picker.setValue(currentValue);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			currentValue = picker.getValue();
			persistInt(currentValue);
		}
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		if (restorePersistedValue) {
			currentValue = getPersistedInt(DEFAULT_VALUE);
		} else {
			currentValue = (Integer) defaultValue;
			persistInt(currentValue);
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInt(index, DEFAULT_VALUE);
	}
}
