package de.onyxbits.drudgery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.NumberPicker;

public class WorkingHoursPicker extends AlertDialog implements OnClickListener {

	private NumberPicker hoursPicker;
	private NumberPicker minutesPicker;
	private MainActivity mainActivity;

	public WorkingHoursPicker(MainActivity context, int hours, int minutes) {
		super(context);
		this.mainActivity = context;

		setIcon(0);
		setTitle(context.getString(R.string.title_set_working_time));

		setButton(BUTTON_POSITIVE, context.getText(android.R.string.ok), this);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.workinghoursdialog, null);
		setView(view);
		hoursPicker = (NumberPicker) view.findViewById(R.id.workinghours);
		minutesPicker = (NumberPicker) view.findViewById(R.id.workingminutes);
		hoursPicker.setMinValue(0);
		hoursPicker.setMaxValue(23);
		hoursPicker.setValue(hours);
		minutesPicker.setMinValue(0);
		minutesPicker.setMaxValue(59);
		minutesPicker.setValue(minutes);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		mainActivity.onWorkingHoursSet(hoursPicker.getValue(),
				minutesPicker.getValue());
	}


}
