package edu.washington.cs.mystatus.activities;

import edu.washington.cs.mystatus.activities.FormDownloadList;
import edu.washington.cs.mystatus.activities.MainMenuActivity;

import edu.washington.cs.mystatus.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

/**
 * MainActivity is a simple main menu for myStatus.
 * 
 * @author Jake Bailey (rjacob@cs.washington.edu)
 */
public class MainActivity extends Activity {

	private static final String TAG = "mystatus.MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mystatus_main);

		Button trackBtn = (Button) findViewById(R.id.button_track);
		Button setupBtn = (Button) findViewById(R.id.button_setup);
		Button planBtn  = (Button) findViewById(R.id.button_plan);
		Button helpBtn  = (Button) findViewById(R.id.button_help);

		trackBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Track button clicked");
				startActivity(new Intent(MainActivity.this, SurveysActivity.class));
			}
		});

		setupBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Setup button clicked");
				startActivity(new Intent(MainActivity.this, FormDownloadList.class));
			}
		});

		planBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Plan button clicked");
				startActivity(new Intent(MainActivity.this, GoalsActivity.class));
			}
		});

		helpBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Help button clicked");
				startActivity(new Intent(MainActivity.this, HelpActivity.class));
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Add items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.action_odk_menu:
			startActivity(new Intent(this, MainMenuActivity.class));
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

}
