package de.onyxbits.drudgery;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.os.Bundle;
import android.provider.AlarmClock;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
		TimePickerDialog.OnTimeSetListener, ViewPager.OnPageChangeListener {

	/**
	 * Maximum number of different worktime settings allowed. Design decision:
	 * three is convenient. Doesn't make the UI too annoying to use, works well
	 * for people doing shift work and is reasonable for people having more than
	 * one job.
	 */
	public static final int MAXSHIFTS = 3;

	public static final String PREFSFILE = "preferences";
	public static final String STARTHOUR = "starthour";
	public static final String STARTMINUTE = "startminute";
	public static final String WORKINGHOURS = "workinghours";
	public static final String WORKINGMINUTES = "workingminutes";
	public static final String WAGEINT = "wageint";
	public static final String WAGEFRAC = "wagefrac";
	public static final String BREAKTIME = "breaktime";
	public static final String HIGHSCORE = "highscore";
	public static final String SHIFT = "shift";

	public static final int DEF_STARTHOUR = 8;
	public static final int DEF_STARTMINUTE = 0;
	public static final int DEF_WORKINGHOURS = 8;
	public static final int DEF_WORKINGMINUTES = 0;
	public static final int DEF_WAGEINT = 7;
	public static final int DEF_WAGEFRAC = 99;
	public static final int DEF_HIGHSCORE = 0;
	public static final int DEF_BREAKTIME = 30;
	public static final int DEF_SHIFT = 0;

	/**
	 * Master configuration file
	 */
	private SharedPreferences prefs;

	/**
	 * Shift configs
	 */
	private SharedPreferences shiftPrefs[] = new SharedPreferences[MAXSHIFTS];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		prefs = getSharedPreferences(PREFSFILE, 0);
		AppRater.appLaunched(this);
		LayoutInflater inflater = getLayoutInflater();
		View content = inflater.inflate(R.layout.activity_main, null);
		List<View> pages = new ArrayList<View>();

		for (int i = 0; i < MAXSHIFTS; i++) {
			shiftPrefs[i] = getSharedPreferences(getShiftPrefsFile(i), 0);
			pages.add(inflater.inflate(R.layout.summaryfragment, null));
		}

		SummaryPageAdapter pagerAdapter = new SummaryPageAdapter(pages);
		ViewPager viewPager = (ViewPager) content.findViewById(R.id.pager);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setCurrentItem(prefs.getInt(SHIFT,DEF_SHIFT));
		viewPager.setOnPageChangeListener(this);
		setContentView(content);
		buildSummary();
	}

	/**
	 * Use this to get the name for getSharedPreferences() when getting shift
	 * preferences.
	 * @param shift shift index
	 * @return the preferences file name.
	 */
	public static String getShiftPrefsFile(int shift) {
		return PREFSFILE+"_shift_"+shift;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		try {
			getPackageManager().getPackageInfo("com.google.zxing.client.android",0);
		}
		catch (Exception exp) {
			menu.getItem(2).setVisible(false);
			menu.getItem(2).setEnabled(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.mi_about_dq: {
				WebView wv = new WebView(this);
				String txt = getString(R.string.game_description);
				wv.loadData(txt, "text/html", null);

				AlertDialog.Builder builder = new AlertDialog.Builder(this);

				builder.setView(wv).setTitle(R.string.mi_about_dq)
						.setPositiveButton(null, null).setNegativeButton(null, null)
						.setNeutralButton(null, null).show();
				return true;
			}
			case R.id.mi_start_break: {
				Intent i = new Intent(AlarmClock.ACTION_SET_ALARM);
				Calendar rightNow = Calendar.getInstance();
				ViewPager pager = (ViewPager) findViewById(R.id.pager);
				int idx = pager.getCurrentItem();
				int blen = shiftPrefs[idx].getInt(BREAKTIME,DEF_BREAKTIME);
				rightNow.add(Calendar.MINUTE, prefs.getInt(BREAKTIME, blen));
				i.putExtra(AlarmClock.EXTRA_MESSAGE, R.string.msg_back_to_work);
				i.putExtra(AlarmClock.EXTRA_HOUR, rightNow.get(Calendar.HOUR_OF_DAY));
				i.putExtra(AlarmClock.EXTRA_MINUTES, rightNow.get(Calendar.MINUTE));
				i.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
				if (getPackageManager().queryIntentActivities(i, 0).size() == 0) {
					Toast.makeText(this, R.string.msg_no_alarmclock, Toast.LENGTH_SHORT)
							.show();
				}
				else {
					startActivity(i);
				}
				return true;
			}
			case R.id.mi_share: {
				Intent intent = new Intent("com.google.zxing.client.android.ENCODE");
				intent.putExtra("ENCODE_TYPE","TEXT_TYPE");
				intent.putExtra("ENCODE_DATA","market://details?id=de.onyxbits.drudgery");
				startActivity(intent);
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	private void buildSummary() {
		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		int idx = pager.getCurrentItem();
		int starthour = shiftPrefs[idx].getInt(STARTHOUR, DEF_STARTHOUR);
		int startminute = shiftPrefs[idx].getInt(STARTMINUTE, DEF_STARTMINUTE);
		int workinghours = shiftPrefs[idx].getInt(WORKINGHOURS, DEF_WORKINGHOURS);
		int workingminutes = shiftPrefs[idx].getInt(WORKINGMINUTES,
				DEF_WORKINGMINUTES);
		int wageint = shiftPrefs[idx].getInt(WAGEINT, DEF_WAGEINT);
		int wagefrac = shiftPrefs[idx].getInt(WAGEFRAC, DEF_WAGEFRAC);
		int breaklength = shiftPrefs[idx].getInt(BREAKTIME, DEF_BREAKTIME);

		Calendar rightNow = Calendar.getInstance();
		rightNow.set(Calendar.HOUR_OF_DAY, starthour);
		rightNow.set(Calendar.MINUTE, startminute);
		NumberFormat nf = NumberFormat.getCurrencyInstance();
		Resources res = getResources();
		String qhours = res.getQuantityString(R.plurals.hours, workinghours,
				workinghours);
		String qmins = res.getQuantityString(R.plurals.minutes, workingminutes,
				workingminutes);
		String qlen = res.getQuantityString(R.plurals.minutes, breaklength,
				breaklength);
		String txt = getString(R.string.lbl_summary, rightNow, qhours, qmins,
				nf.format(((wageint * 100) + wagefrac) / 100d), qlen);
		SummaryPageAdapter adapter = (SummaryPageAdapter) pager.getAdapter();
		View view = adapter.getItemAt(idx);
		((TextView) view.findViewById(R.id.lbl_summary)).setText(txt);
		Intent intent = new Intent(this, UpdateService.class);
		intent.setAction("START_APP");
		startService(intent);
	}

	public void showWorkingHours(View view) {
		int idx = ((ViewPager) findViewById(R.id.pager)).getCurrentItem();
		int hour = shiftPrefs[idx].getInt(WORKINGHOURS, DEF_WORKINGHOURS);
		int minute = shiftPrefs[idx].getInt(WORKINGMINUTES, DEF_WORKINGMINUTES);
		new WorkingHoursPicker(this, hour, minute).show();
	}

	public void showWages(View view) {
		int idx = ((ViewPager) findViewById(R.id.pager)).getCurrentItem();
		new WagePicker(this, shiftPrefs[idx].getInt(WAGEINT, DEF_WAGEINT),
				prefs.getInt(WAGEFRAC, DEF_WAGEFRAC)).show();
	}

	public void showStartOfWork(View view) {
		int idx = ((ViewPager) findViewById(R.id.pager)).getCurrentItem();
		int hour = shiftPrefs[idx].getInt(STARTHOUR, DEF_STARTHOUR);
		int minute = shiftPrefs[idx].getInt(STARTMINUTE, DEF_STARTMINUTE);

		new TimePickerDialog(this, this, hour, minute,
				DateFormat.is24HourFormat(this)).show();
	}

	public void showBreakLength(View view) {
		int idx = ((ViewPager) findViewById(R.id.pager)).getCurrentItem();
		int blen = shiftPrefs[idx].getInt(BREAKTIME, DEF_BREAKTIME);
		new BreakLengthPicker(this,blen).show();
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		int idx = ((ViewPager) findViewById(R.id.pager)).getCurrentItem();
		Editor editor = shiftPrefs[idx].edit();
		editor.putInt(STARTHOUR, hourOfDay);
		editor.putInt(STARTMINUTE, minute);
		editor.apply();
		buildSummary();
	}

	protected void onWorkingHoursSet(int hours, int minutes) {
		int idx = ((ViewPager) findViewById(R.id.pager)).getCurrentItem();
		Editor editor = shiftPrefs[idx].edit();
		editor.putInt(WORKINGHOURS, hours);
		editor.putInt(WORKINGMINUTES, minutes);
		editor.apply();
		buildSummary();
	}

	protected void onWageSet(int wageint, int wagefrac) {
		int idx = ((ViewPager) findViewById(R.id.pager)).getCurrentItem();
		Editor editor = shiftPrefs[idx].edit();
		editor.putInt(WAGEINT, wageint);
		editor.putInt(WAGEFRAC, wagefrac);
		editor.apply();
		buildSummary();
	}

	protected void onBreakLengthSet(int length) {
		int idx = ((ViewPager) findViewById(R.id.pager)).getCurrentItem();
		Editor editor = shiftPrefs[idx].edit();
		editor.putInt(BREAKTIME, length);
		editor.apply();
		buildSummary();
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int arg0) {
		Editor editor = prefs.edit();
		editor.putInt(SHIFT, arg0);
		editor.apply();
		buildSummary();
	}

}
