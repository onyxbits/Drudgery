package de.onyxbits.drudgery;

import android.content.Intent;
import android.util.Log;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

/**
 * Ties Drudgery into the Dashclock widget. This is as primitive as it can get.
 * Whenever drudgery is added to the dashclock, this class will start the
 * updateservice, the updateservice in turn periodically sends us an intent with
 * a ready to publish title and body.
 * 
 * @author patrick
 * 
 */
public class DashclockExtensionService extends DashClockExtension {

	public static final String DASH_TITLE = "DASH_TITLE";
	public static final String DASH_BODY = "DASH_BODY";

	public DashclockExtensionService() {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String title = intent.getStringExtra(DASH_TITLE);
		String body = intent.getStringExtra(DASH_BODY);
		try {
			publishUpdate(new ExtensionData().visible(true)
					.icon(R.drawable.ic_extension).status(title).expandedTitle(title)
					.expandedBody(body)
					.clickIntent(new Intent(this, ProxyActivity.class)));
		}
		catch (Exception exp) {
			// This can happen if the UpdateService is running, Dashclock is
			// installed, but the widget is not added to Dashclock.
		}
		return START_NOT_STICKY;
	}

	@Override
	protected void onInitialize(boolean isReconnect) {
		Intent intent = new Intent(this, UpdateService.class);
		intent.setAction("START_DASH");
		startService(intent);
	}

	@Override
	protected void onUpdateData(int arg0) {
	}

}
