package de.onyxbits.drudgery;

import java.util.Calendar;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Shader.TileMode;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

/**
 * The backend of the widget. The service is implemented in a "game loop"
 * fashion. When created, it will register itself with the alarmmanger to tick
 * itself on top of every minute. Removing all widgets cancels the alarm.
 * 
 * @author patrick
 * 
 */
public class UpdateService extends Service {

	/**
	 * Send this action to make the game toggle it's selection state.
	 */
	public static final String ACTION_TOGGLE = "TOGGLE";

	/**
	 * Update interval in milliseconds
	 */
	public static final int INTERVAL = 60 * 1000;

	private SharedPreferences prefs;
	private SharedPreferences shiftPrefs;
	private ComponentName myWidget;
	private AppWidgetManager widgetManager;
	private Game game;
	private TimeUtil timeUtil;

	@Override
	public void onCreate() {
		prefs = getSharedPreferences(MainActivity.PREFSFILE, 0);
		int shift = prefs.getInt(MainActivity.SHIFT, MainActivity.DEF_SHIFT);
		shiftPrefs = getSharedPreferences(MainActivity.getShiftPrefsFile(shift), 0);
		myWidget = new ComponentName(this, DrudgeryWidgetProvider.class);
		widgetManager = AppWidgetManager.getInstance(this);
		timeUtil = new TimeUtil(shiftPrefs);
		game = new Game(prefs.getInt(MainActivity.HIGHSCORE,
				MainActivity.DEF_HIGHSCORE), getResources());
		toggleAlarm(false);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		boolean required = false; // Is a widget using us?

		int shift = prefs.getInt(MainActivity.SHIFT, MainActivity.DEF_SHIFT);
		shiftPrefs = getSharedPreferences(MainActivity.getShiftPrefsFile(shift), 0);
		timeUtil.update(shiftPrefs);
		Log.d(getClass().getName(), "" + intent.getAction());
		if (intent != null && ACTION_TOGGLE.equals(intent.getAction())) {
			game.nextChoice = !game.nextChoice;
		}

		if (game.round > timeUtil.round) {
			// First round of the day.
			game = new Game(prefs.getInt(MainActivity.HIGHSCORE,
					MainActivity.DEF_HIGHSCORE), getResources());
		}
		if (game.score > game.highscore) {
			prefs.edit().putInt(MainActivity.HIGHSCORE, game.score).commit();
			game.highscore = game.score;
		}

		while (timeUtil.round > game.round) {
			// We simply fast forward this. It is very likely that we missed rounds
			// because the device was asleep in which case it is only fair to compute
			// score on the user's last accept/reject preference.
			game.newRound();
		}

		if (widgetManager.getAppWidgetIds(myWidget).length != 0) {
			required = true;
			buildWidgetUpdate(timeUtil);
		}

		ActivityManager mgr = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : mgr.getRunningServices(Integer.MAX_VALUE)) {
			if (DashclockExtensionService.class.getName().equals(
					service.service.getClassName())) {
				required = true;
				buildDashUpdate(timeUtil);
			}
		}

		if (required) {
			return START_STICKY;
		}
		else {
			toggleAlarm(true);
			stopSelf();
			return START_NOT_STICKY;
		}
	}

	private void buildDashUpdate(TimeUtil timeUtil) {
		String title = "";
		String body = "";
		if (timeUtil.working) {
			title = getString(R.string.dashclock_title_work, timeUtil.timeLeft,
					timeUtil.earnings, game.round);
			body = game.buildQuestMessage();
		}
		else {
			title = getString(R.string.title_free_time);
			body = game.buildClosingTimeMessage();
		}
		Intent dint = new Intent(this, DashclockExtensionService.class);
		dint.putExtra(DashclockExtensionService.DASH_TITLE, title);
		dint.putExtra(DashclockExtensionService.DASH_BODY, body);
		startService(dint);
	}

	private void buildWidgetUpdate(TimeUtil timeUtil) {
		RemoteViews view = new RemoteViews(getPackageName(), R.layout.appwidget);
		if (timeUtil.working) {
			view.setViewVisibility(R.id.img_freetime, View.GONE);
			view.setViewVisibility(R.id.timeleft, View.VISIBLE);

			view.setTextViewText(R.id.moneyearned,
					getString(R.string.lbl_moneyearned, timeUtil.earnings));
			view.setTextViewText(R.id.timeleft,
					getString(R.string.lbl_timeleft, timeUtil.timeLeft));
			view.setTextViewText(R.id.lbl_quest_title,
					getString(R.string.lbl_quest_title, game.round));
			view.setTextViewText(R.id.lbl_quest_body, game.buildQuestMessage());

			Intent intent = new Intent(this, UpdateService.class);
			intent.setAction(ACTION_TOGGLE);
			view.setOnClickPendingIntent(R.id.gamecontainer, PendingIntent
					.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
		}
		else {
			view.setViewVisibility(R.id.timeleft, View.GONE);
			view.setViewVisibility(R.id.img_freetime, View.VISIBLE);
			view.setTextViewText(R.id.moneyearned,
					getString(R.string.lbl_moneyearned, timeUtil.totalEarnings));
			view.setTextViewText(R.id.lbl_quest_title,
					getString(R.string.title_free_time));
			view.setTextViewText(R.id.lbl_quest_body, game.buildClosingTimeMessage());

		}
		view.setOnClickPendingIntent(R.id.timecontainer, PendingIntent.getActivity(
				this, 0, new Intent(this, MainActivity.class),
				PendingIntent.FLAG_UPDATE_CURRENT));
		widgetManager.updateAppWidget(myWidget, view);
	}

	/**
	 * Add or remove an alarm that will periodically tick this service
	 * 
	 * @param cancel
	 *          true to cancel the alarm, false to set the alarm.
	 */
	private void toggleAlarm(boolean cancel) {
		AlarmManager m = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(this, UpdateService.class);
		intent.setAction("TICK");
		PendingIntent service = PendingIntent.getService(this, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		if (cancel) {
			m.cancel(service);
			Log.d(getClass().getName(), "Service cancelled");
		}
		else {
			Calendar time = Calendar.getInstance();
			time.set(Calendar.SECOND, 0);
			time.set(Calendar.MILLISECOND, 0);
			m.setRepeating(AlarmManager.RTC, time.getTimeInMillis(), INTERVAL,
					service);
			Log.d(getClass().getName(), "Service scheduled");
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
