package edu.washington.cs.mystatus.activities;

import android.app.TabActivity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TabHost;
import edu.washington.cs.mystatus.R;
import edu.washington.cs.mystatus.application.MyStatus;
import edu.washington.cs.mystatus.receivers.ScreenOnOffReceiver;

/**
 * Displays tabs for the list of passive surveys. Shows one tab for the surveys
 * that are currently due, and another for a list of all surveys.
 * 
 * @author Jake Bailey (rjacob@cs.washington.edu)
 */
@SuppressWarnings("deprecation")
public class SurveyListTabs extends TabActivity {

	private static final String DUE_TAB = "due_tab";
	private static final String ALL_TAB = "all_tab";
	// Added tab for history ... just put it there for now....
	// @CD
	private static final String HISTORY_TAB = "history_tab";
	private ScreenOnOffReceiver screenReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setTitle(getString(R.string.app_name));
		// adding screen on off receiver for turning off the screen correctly
		// @CD
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		screenReceiver = new ScreenOnOffReceiver();
		registerReceiver(screenReceiver, intentFilter);
		final TabHost tabHost = getTabHost();

		Intent dueList = new Intent(this, DueSurveysList.class);
		tabHost.addTab(tabHost.newTabSpec(DUE_TAB).setIndicator(getString(R.string.due_surveys))
				.setContent(dueList));

		Intent allList = new Intent(this, AllSurveysList.class);
		tabHost.addTab(tabHost.newTabSpec(ALL_TAB).setIndicator(getString(R.string.all_surveys))
				.setContent(allList));
		// Added tab for history ... just put it there for now....
		// @CD
		Intent historyList = new Intent (this, HistoryActivity.class);
		tabHost.addTab(tabHost.newTabSpec(HISTORY_TAB).setIndicator(getString(R.string.history))
				.setContent(historyList));
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//screen is off and should be lock
        if (screenReceiver.wasOffBefore){
        	((MyStatus)getApplicationContext()).getCacheWordHandler().manuallyLock();
        	MyStatus.cleanUpTemporaryFiles();
        	finish();
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
