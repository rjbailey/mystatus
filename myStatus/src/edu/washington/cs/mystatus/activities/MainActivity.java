package edu.washington.cs.mystatus.activities;

import info.guardianproject.cacheword.CacheWordActivityHandler;
import info.guardianproject.cacheword.ICacheWordSubscriber;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import edu.washington.cs.mystatus.R;
import edu.washington.cs.mystatus.application.MyStatus;
import edu.washington.cs.mystatus.providers.FormsProviderAPI.FormsColumns;
import edu.washington.cs.mystatus.providers.InstanceProviderAPI.InstanceColumns;
import edu.washington.cs.mystatus.utilities.FileUtils;


/**
 * MainActivity is a simple main menu for myStatus.
 * 
 * @author Jake Bailey (rjacob@cs.washington.edu)
 */
public class MainActivity extends Activity implements ICacheWordSubscriber {

	private static final String TAG = "mystatus.MainActivity";

	private CacheWordActivityHandler mCacheWord;
	private boolean firstTimeInitialize = false;

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
		//mCacheWord.connectToService();
		trackBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Track button clicked");
				((MyStatus)getApplicationContext()).connectCacheWord();
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
			((MyStatus)getApplicationContext()).connectCacheWord();
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
		// clear out all the old history
		// @CD
		// clean up temp folder
		File f1 = new File (MyStatus.FORMS_PATH);
		File f2 = new File (MyStatus.INSTANCES_PATH);
		File f3 = new File (MyStatus.METADATA_PATH);
		FileUtils.deleteAllFilesInDirectoryRecursively(f1);
		FileUtils.deleteAllFilesInDirectoryRecursively(f2);
		FileUtils.deleteAllFilesInDirectoryRecursively(f3);
		firstTimeInitialize = true;
		showLockScreen();
	}

	@Override
	public void onCacheWordLocked() {
		// such as close database and erase all decrypted media files
		// @CD
		// clean up temp folder
		cleanUpTemporaryFiles();
	}
	
	//TODO: might need to figure out how to support api8
	// but it's hard otherwise we have to modify the content provider
	// which will be not good as long term it would be hard to modify.
    @Override
	public void onCacheWordOpened() {
		// reset database for first time log in 
	    // @CD
	    if (firstTimeInitialize){
	        MyStatus.createODKDirs();
	        getContentResolver().update(FormsColumns.CONTENT_URI, null, "resetDb", null);
	        getContentResolver().update(InstanceColumns.CONTENT_URI, null, "resetDb", null);
	        firstTimeInitialize = false;
	    }
		
	}
	
	// disconnect cacheword service
	// we have to do this as cacheword wont trigger timeout 
	// until we dont have any more subscribers.
	// @CD
	@Override
    protected void onPause() {
        super.onPause();
        mCacheWord.onPause();
    }
	
	// reconnect cache word service after disconnect
	// @CD
    @Override
    protected void onResume() {
        super.onResume();
        mCacheWord.onResume();
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (mCacheWord.isLocked() && hasFocus){
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
    
    // make sure all the temporary files got clean up 
 	// right after the app destroy
 	// @CD
	@Override
	protected void onDestroy() {
		cleanUpTemporaryFiles();
		super.onDestroy();	
	}
	
	/**
	 * Helper used to clean up all files and folder under the temp folder
	 * @CD
	 */
	private void cleanUpTemporaryFiles(){
		File f = new File (MyStatus.TEMP_MEDIA_PATH);
		FileUtils.deleteAllFilesInDirectoryRecursively(f);
	}

	

}
