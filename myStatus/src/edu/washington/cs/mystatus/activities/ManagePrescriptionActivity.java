package edu.washington.cs.mystatus.activities;

import edu.washington.cs.mystatus.R;
import edu.washington.cs.mystatus.R.layout;
import edu.washington.cs.mystatus.R.menu;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;

/**
 * 
 * @author Emily Chien (eechien@cs.washington.edu)
 *
 */

public class ManagePrescriptionActivity extends Activity {

	private final String TAG = "myStatus.ManagePrescriptionActivity";
	
	//private LinearLayout mPresList;
	private Button mManagePres; //replaced later by entries from db
	private Button mAddNewPres;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_prescription);
		// Show the Up button in the action bar.
		
		mManagePres = (Button) findViewById(R.id.a_pres);
		mAddNewPres = (Button) findViewById(R.id.add_new_prescription);
		
		mManagePres.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "manage prescription button clicked");
			}
		});
		
		mAddNewPres.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "add new prescription button clicked");
				startActivity(new Intent(ManagePrescriptionActivity.this, AddPrescription.class));
			}
		});
	}
}
