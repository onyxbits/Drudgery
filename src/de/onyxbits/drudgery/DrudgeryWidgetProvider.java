package de.onyxbits.drudgery;

import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

/**
 * We need updates every minute. The standard widget update mechanism therefore 
 * won't do. Instead, we start the updateservice when a widget gets added to
 * the launcher. That service then restarts itself every minute by using 
 * the alarmmanager.
 * @author patrick
 *
 */
public class DrudgeryWidgetProvider extends AppWidgetProvider {

	public DrudgeryWidgetProvider() {
	}

	@Override
	public void onEnabled(Context context) {
		Intent intent = new Intent(context,UpdateService.class);
		intent.setAction("START_WIDGET");
		context.startService(intent);
	}
}
