package de.onyxbits.drudgery;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.TextView;

public class WagePicker extends AlertDialog implements OnClickListener {

	private NumberPicker integerPicker;
	private NumberPicker fractionPicker;
	private MainActivity mainActivity;

	public WagePicker(MainActivity context, int wageint, int wagefrac) {
		super(context);
		this.mainActivity = context;
		DecimalFormatSymbols syms = DecimalFormatSymbols.getInstance(Locale.getDefault());

		setIcon(0);
		setTitle(context.getString(R.string.title_set_hourly_earnings));

		setButton(BUTTON_POSITIVE, context.getText(android.R.string.ok), this);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.wagedialog, null);
		setView(view);
		integerPicker = (NumberPicker) view.findViewById(R.id.wage_integer);
		fractionPicker = (NumberPicker) view.findViewById(R.id.wage_fraction);
		integerPicker.setMinValue(0);
		integerPicker.setMaxValue(99);
		integerPicker.setValue(wageint);
		fractionPicker.setMinValue(0);
		fractionPicker.setMaxValue(99);
		fractionPicker.setValue(wagefrac);
		((TextView)view.findViewById(R.id.decimalpoint)).setText(""+syms.getDecimalSeparator());
		((TextView)view.findViewById(R.id.currency)).setText(""+syms.getCurrencySymbol());
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		mainActivity.onWageSet(integerPicker.getValue(),fractionPicker.getValue());
	}


}
