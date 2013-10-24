package de.onyxbits.drudgery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.NumberPicker;

public class BreakLengthPicker extends AlertDialog implements OnClickListener {

	private NumberPicker minutePicker;
	private MainActivity mainActivity;

	public BreakLengthPicker(MainActivity context, int length) {
		super(context);
		this.mainActivity = context;

		setIcon(0);
		setTitle(context.getString(R.string.title_set_break_length));

		setButton(BUTTON_POSITIVE, context.getText(android.R.string.ok), this);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.breakdialog, null);
		setView(view);
		minutePicker = (NumberPicker) view.findViewById(R.id.breaklengthpicker);
		minutePicker.setMinValue(0);
		minutePicker.setMaxValue(60);
		minutePicker.setValue(length);
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		mainActivity.onBreakLengthSet(minutePicker.getValue());
	}


}
