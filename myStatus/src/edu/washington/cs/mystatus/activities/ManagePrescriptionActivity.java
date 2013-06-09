package edu.washington.cs.mystatus.activities;

import java.util.List;

import edu.washington.cs.mystatus.R;
import edu.washington.cs.mystatus.R.layout;
import edu.washington.cs.mystatus.R.menu;
import edu.washington.cs.mystatus.application.MyStatus;
import edu.washington.cs.mystatus.database.PrescriptionOpenHelper;
import edu.washington.cs.mystatus.receivers.ScreenOnOffReceiver;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

/**
 * Creates the layout that displays previously added prescriptions
 * and a button to add a new prescription.
 * 
 * @author Emily Chien (eechien@cs.washington.edu)
 *
 */

public class ManagePrescriptionActivity extends Activity {

	private final String TAG = "myStatus.ManagePrescriptionActivity";
	
	private Button mAddNewPres;
	private LinearLayout mCurrPres;
	private ScreenOnOffReceiver screenReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_prescription);
		
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		screenReceiver = new ScreenOnOffReceiver();
		registerReceiver(screenReceiver, intentFilter);
		
		mCurrPres = (LinearLayout) findViewById(R.id.current_prescriptions);
		
		mAddNewPres = (Button) findViewById(R.id.add_new_prescription);
		
		mAddNewPres.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "add new prescription button clicked");
				startActivity(new Intent(ManagePrescriptionActivity.this, AddPrescription.class));
			}
		});
		
		addCurrentPrescriptions();
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		setContentView(R.layout.activity_manage_prescription);
		mCurrPres = (LinearLayout) findViewById(R.id.current_prescriptions);
		
		mAddNewPres = (Button) findViewById(R.id.add_new_prescription);
		
		mAddNewPres.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "add new prescription button clicked");
				startActivity(new Intent(ManagePrescriptionActivity.this, AddPrescription.class));
			}
		});
		
		addCurrentPrescriptions();
	}
	
	// dynamically adds buttons for the prescriptions already added by the user
	private void addCurrentPrescriptions() {
		PrescriptionOpenHelper helper = new PrescriptionOpenHelper(this);
		List<String> names = helper.getBrandChemFilenames();
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		for (int i = 0; i < names.size(); i = i + 3) {
			Button presButton = new Button(ManagePrescriptionActivity.this);
			presButton.setLayoutParams(lp);
			final String filename = names.get(i + 2);
			Bitmap bitmap = decodeSampledBitmapFromResource(filename, 200, 200);
			Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 200, 200, true));
			presButton.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
			final String brandName = names.get(i);
			final String chemName = names.get(i + 1);
			presButton.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			presButton.setText(brandName + " (" + chemName + ")");
			presButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d(TAG, "Adding prescription button.");
					Intent intent = new Intent(ManagePrescriptionActivity.this, AddPrescription.class);
					// add brand name and chemical name to the intent so when the activity
					// is generated the correct prescription will display
					Bundle bundle = new Bundle();
					bundle.putString("BRAND_NAME", brandName);
					bundle.putString("CHEM_NAME", chemName);
					bundle.putString("FILENAME", filename);
					intent.putExtras(bundle);
					startActivity(intent);
				}
			});
			mCurrPres.addView(presButton);
		}
			
	}
	
	// for making the image smaller
	private Bitmap decodeSampledBitmapFromResource(String pathName,
	        int reqWidth, int reqHeight) {
	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(pathName, options);
	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeFile(pathName, options);
	}
	
	// for making the image smaller
	private int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	
	        // Calculate ratios of height and width to requested height and width
	        final int heightRatio = Math.round((float) height / (float) reqHeight);
	        final int widthRatio = Math.round((float) width / (float) reqWidth);
	
	        // Choose the smallest ratio as inSampleSize value, this will guarantee
	        // a final image with both dimensions larger than or equal to the
	        // requested height and width.
	        inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
	    }
	
	    return inSampleSize;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		((MyStatus)getApplicationContext()).disconnectCacheWord();
	}

	@Override
	protected void onResume() {
		super.onResume();
		//screen is off and should be lock
        if (screenReceiver.wasOffBefore){
        	((MyStatus)getApplicationContext()).getCacheWordHandler().manuallyLock();
        	MyStatus.cleanUpTemporaryFiles();
        	finish();
        }
        ((MyStatus)getApplicationContext()).connectCacheWord();
	}
}
