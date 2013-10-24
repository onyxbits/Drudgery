package de.onyxbits.drudgery;

import android.os.Bundle;
import android.view.View;
import android.app.Activity;
import android.content.Intent;

/**
 * Dirty hack/workaround for Dashclock: The widget can only start activities,
 * so we wrap the service start in a UI-less activity.
 * @author patrick
 *
 */
public class ProxyActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(new View(this));
		Intent intent = new Intent(this,UpdateService.class);
		intent.setAction(UpdateService.ACTION_TOGGLE);
		startService(intent);
		finish();
	}

}
