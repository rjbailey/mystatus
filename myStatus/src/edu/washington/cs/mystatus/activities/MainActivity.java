package edu.washington.cs.mystatus.activities;

import info.guardianproject.cacheword.CacheWordActivityHandler;
import info.guardianproject.cacheword.CacheWordHandler;
import info.guardianproject.cacheword.ICacheWordSubscriber;
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

/**
 * MainActivity is a simple main menu for myStatus.
 * 
 * @author Jake Bailey (rjacob@cs.washington.edu)
 */
public class MainActivity extends Activity implements ICacheWordSubscriber {

	private static final String TAG = "mystatus.MainActivity";

	private CacheWordActivityHandler mCacheWord;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mystatus_main);

		View trackBtn = findViewById(R.id.button_track);
		View setupBtn = findViewById(R.id.button_setup);
		View planBtn  = findViewById(R.id.button_plan);
		View helpBtn  = findViewById(R.id.button_help);

		// adding activity handler
		mCacheWord = new CacheWordActivityHandler(this);


		trackBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Track button clicked");
				startActivity(new Intent(MainActivity.this, SurveyListTabs.class));
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCacheWord.onResume();
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
