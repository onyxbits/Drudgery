package de.onyxbits.drudgery;

import java.text.NumberFormat;
import java.util.Calendar;

import android.content.SharedPreferences;

/**
 * Utility class for doing the time calculation.
 * 
 * @author patrick
 * 
 */
public class TimeUtil {

	/**
	 * Time per round in milliseconds;
	 */
	public static final long ROUNDLENGTH = 5 * 60 * 1000;

	/**
	 * Time as it was when creating the object.
	 */
	public Calendar start, end, now;

	/**
	 * Worktime or free time?
	 */
	public boolean working;

	/**
	 * Total time to work in milliseconds;
	 */
	public long worktime;

	/**
	 * How much time was spend at work so far (milliseconds);
	 */
	public long workedtime;

	/**
	 * How much longer to stay at work (milliseconds)
	 */
	public long remaining;

	/**
	 * Remaining time in hours
	 */
	public long hours;

	/**
	 * Remaining time in minutes
	 */
	public long minutes;

	/**
	 * Current round in the game.
	 */
	public int round;

	/**
	 * Payout for the time worked, correctly formated with currency symbol.
	 */
	public String earnings;

	/**
	 * Payout for the day, correctly formated with currency symbol.
	 */
	public String totalEarnings;

	/**
	 * Remaining time for human consumption
	 */
	public String timeLeft;

	/**
	 * Construct a new object and do the time calculations for the current time.
	 * 
	 * @param prefs
	 *          prefs file from which to obtain wages, start and end time.
	 */
	public TimeUtil(SharedPreferences prefs) {
		update(prefs);
	}

	/**
	 * Recalculate
	 */
	protected void update(SharedPreferences prefs) {
		Calendar start, end, now;

		now = Calendar.getInstance();
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);
		start = (Calendar) now.clone();
		start.set(Calendar.HOUR_OF_DAY,
				prefs.getInt(MainActivity.STARTHOUR, MainActivity.DEF_STARTHOUR));
		start.set(Calendar.MINUTE,
				prefs.getInt(MainActivity.STARTMINUTE, MainActivity.DEF_STARTMINUTE));
		end = (Calendar) start.clone();
		end.add(Calendar.HOUR_OF_DAY,
				prefs.getInt(MainActivity.WORKINGHOURS, MainActivity.DEF_WORKINGHOURS));
		end.add(Calendar.MINUTE, prefs.getInt(MainActivity.WORKINGMINUTES,
				MainActivity.DEF_WORKINGMINUTES));

		working = (now.after(start) && now.before(end));
		if (!working) {
			// Check for a nightshift that goes over midnight
			start.add(Calendar.HOUR_OF_DAY, -24);
			end.add(Calendar.HOUR_OF_DAY, -24);
			working = (now.after(start) && now.before(end));
		}

		worktime = end.getTimeInMillis() - start.getTimeInMillis();
		workedtime = now.getTimeInMillis() - start.getTimeInMillis();
		remaining = worktime - workedtime;
		hours = (remaining / 1000) / (60 * 60);
		minutes = ((remaining / 1000) % (60 * 60)) / 60;

		int iVal = prefs.getInt(MainActivity.WAGEINT, MainActivity.DEF_WAGEINT);
		int fVal = prefs.getInt(MainActivity.WAGEFRAC, MainActivity.DEF_WAGEFRAC);
		double perSec = (((iVal * 100) + fVal) / 100d) / 3600d;
		NumberFormat nf = NumberFormat.getCurrencyInstance();
		earnings = nf.format(perSec * (workedtime / 1000));
		totalEarnings = nf.format(perSec * (worktime / 1000));
		timeLeft = String.format("%01d:%02d", hours, minutes);

		long maxrounds = worktime / ROUNDLENGTH;
		if (working) {

			round = (int) (((double) maxrounds) * (((double) workedtime) / ((double) worktime))) + 1;
		}
		else {
			round = (int) maxrounds;
		}
	}

}
