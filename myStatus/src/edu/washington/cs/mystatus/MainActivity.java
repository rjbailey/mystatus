package edu.washington.cs.mystatus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	private static final String TAG = "myStatus";

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
				Log.d(TAG, "Information button clicked");
			}
		});

		mHelpBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Help button clicked");
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
