package edu.washington.cs.mystatus.activities;

import info.guardianproject.cacheword.CacheWordActivityHandler;
import info.guardianproject.cacheword.CacheWordHandler;
import info.guardianproject.cacheword.ICacheWordSubscriber;
import edu.washington.cs.mystatus.activities.FormDownloadList;
import edu.washington.cs.mystatus.activities.MainMenuActivity;
import edu.washington.cs.mystatus.application.MyStatus;

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
public class MainActivity extends Activity implements ICacheWordSubscriber {

	private static final String TAG = "mystatus.MainActivity";

	private Button mSurveyBtn;
	private Button mHistoryBtn;
	private Button mGoalsBtn;
	private Button mManageSurveysBtn;
	private Button mInformationBtn;
	private Button mHelpBtn;
	private CacheWordActivityHandler mCacheWord;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// adding activity handler
		mCacheWord = new CacheWordActivityHandler(this);
		
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
				startActivity(new Intent(MainActivity.this, FormDownloadList.class));
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
			startActivity(new Intent(this, MainMenuActivity.class));
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}
	
	// methods need to be added to subscribed to cache word
	// @CD
	@Override
	public void onCacheWordUninitialized() {
		showLockScreen();
	}

	@Override
	public void onCacheWordLocked() {
		// TODO: might need to do some more clean up here
		// such as close database and erase all decrypted media files
		// @CD
		showLockScreen();
	}

	@Override
	public void onCacheWordOpened() {
		// TODO: night need to reenable database and 
		// @CD
		
	}
	
	@Override
    protected void onPause() {
        super.onPause();
        mCacheWord.onPause();
        //((MyStatus)getApplicationContext()).disconnectCacheWord();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCacheWord.onResume();
        ((MyStatus)getApplicationContext()).connectCacheWord();
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
