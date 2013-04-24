package edu.washington.cs.mystatus;

import org.odk.collect.android.activities.FormChooserList;
import org.odk.collect.android.activities.FormDownloadList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
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
	private Button mSettingsBtn;
	private Button mInformationBtn;
	private Button mHelpBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mSurveyBtn = (Button) findViewById(R.id.button_survey);
		mHistoryBtn = (Button) findViewById(R.id.button_history);
		mGoalsBtn = (Button) findViewById(R.id.button_goals);
		mSettingsBtn = (Button) findViewById(R.id.button_settings);
		mInformationBtn = (Button) findViewById(R.id.button_information);
		mHelpBtn = (Button) findViewById(R.id.button_help);

		mSurveyBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Survey button clicked");
				//startActivity(new Intent(MainActivity.this, SurveysActivity.class));
				startActivity(new Intent(MainActivity.this, FormChooserList.class));
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
			}
		});

		mSettingsBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Settings button clicked");
				startActivity(new Intent(MainActivity.this, SettingsActivity.class));
			}
		});

		mInformationBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, FormDownloadList.class));
				Log.d(TAG, "Information button clicked");
			}
		});

		mHelpBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Help button clicked");
				startActivity(OdkProxy.createFormDownloadIntent(MainActivity.this));
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
