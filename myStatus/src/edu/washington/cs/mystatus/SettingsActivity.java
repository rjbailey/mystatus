package edu.washington.cs.mystatus;

import android.app.Activity;
import android.os.Bundle;

/**
 * SettingsActivity provides a UI for managing notification settings.
 * 
 * @author Jake Bailey (rjacob@cs.washington.edu)
 */
public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
	}

}
