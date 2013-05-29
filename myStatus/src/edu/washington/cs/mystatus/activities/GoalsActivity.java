package edu.washington.cs.mystatus.activities;

import edu.washington.cs.mystatus.R;
import edu.washington.cs.mystatus.application.MyStatus;
import edu.washington.cs.mystatus.receivers.ScreenOnOffReceiver;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
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

	private ScreenOnOffReceiver screenReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.mystatus_goals);
		
		setGlobalVariables();
		
		addListenersOnButtons();
		
		// adding screen on off receiver for turning off the screen correctly
		// @CD
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		screenReceiver = new ScreenOnOffReceiver();
		registerReceiver(screenReceiver, intentFilter);
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
				Log.d(TAG, "Manage Prescriptions button clicked");
				startActivity(new Intent(GoalsActivity.this, ManagePrescriptionActivity.class));
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
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		 // disconnect to cache word to get 
        // @CD
        ((MyStatus)getApplicationContext()).disconnectCacheWord();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		 // connect to cache word to get 
        // @CD
        ((MyStatus)getApplicationContext()).connectCacheWord();
        //screen is off and should be lock
        if (screenReceiver.wasOffBefore){
        	((MyStatus)getApplicationContext()).getCacheWordHandler().manuallyLock();
        	MyStatus.cleanUpTemporaryFiles();
        	finish();
        }
	}
	
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {		
		 if (((MyStatus)getApplicationContext()).getCacheWordHandler().isLocked() && hasFocus){
	            showLockScreen();
	        } 
	}
	
	/**
     * show lock screen if not yet initialized
     */
    void showLockScreen() {
        Intent intent = new Intent(this, LockScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("originalIntent", getIntent());
        startActivity(intent);
        finish();
    }
}
