package edu.washington.cs.mystatus;

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

	private Button mSurveyBtn;
	private Button mHistoryBtn;
	private Button mGoalsBtn;
	private Button mManageSurveysBtn;
	private Button mInformationBtn;
	private Button mHelpBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mSurveyBtn = (Button) findViewById(R.id.button_survey);
		mHistoryBtn = (Button) findViewById(R.id.button_history);
		mGoalsBtn = (Button) findViewById(R.id.button_goals);
		mManageSurveysBtn = (Button) findViewById(R.id.button_manage_surveys);
		mInformationBtn = (Button) findViewById(R.id.button_information);
		mHelpBtn = (Button) findViewById(R.id.button_help);

		mSurveyBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Survey button clicked");
				startActivity(new Intent(MainActivity.this, SurveysActivity.class));
			}
		});

		mHistoryBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "History button clicked");
			}
		});

		mGoalsBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Goals button clicked");
				startActivity(new Intent(MainActivity.this, GoalsActivity.class));
			}
		});

		mManageSurveysBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Manage Surveys button clicked");
				startActivity(OdkProxy.createFormDownloadIntent(MainActivity.this));
			}
		});

		mInformationBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Information button clicked");
			}
		});

		mHelpBtn.setOnClickListener(new View.OnClickListener() {
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
			startActivity(OdkProxy.createMainMenuIntent(this));
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

}
