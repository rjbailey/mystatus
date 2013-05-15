package edu.washington.cs.mystatus.activities;

import edu.washington.cs.mystatus.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * 
 * 
 * @author eechien@cs.washington.edu
 */

public class GoalsActivity extends Activity {

	private static final String TAG = "mystatus.CreateEventActivity";
	
	private Button mCreateActivity, mPrescriptions, mSideEffects;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.mystatus_goals);
		
		setGlobalVariables();
		
		addListenersOnButtons();
	}

	private void addListenersOnButtons() {
		mCreateActivity.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Create event button clicked");
				startActivity(new Intent(GoalsActivity.this, CreateEventActivity.class));
			}
		});
		
		mPrescriptions.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
			}
		});
		
		mSideEffects.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});
	}
	
	private void setGlobalVariables() {
		mCreateActivity = (Button) findViewById(R.id.create_activity_button);
		mPrescriptions = (Button) findViewById(R.id.new_prescription_button);
		mSideEffects = (Button) findViewById(R.id.side_effects_button);
	}
}
