package edu.washington.cs.mystatus.activities;

import java.util.List;

import edu.washington.cs.mystatus.R;
import edu.washington.cs.mystatus.R.layout;
import edu.washington.cs.mystatus.R.menu;
import edu.washington.cs.mystatus.database.PrescriptionOpenHelper;
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
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

/**
 * 
 * @author Emily Chien (eechien@cs.washington.edu)
 *
 */

public class ManagePrescriptionActivity extends Activity {

	private final String TAG = "myStatus.ManagePrescriptionActivity";
	
	//private LinearLayout mPresList;
	//private Button mManagePres; //replaced later by entries from db
	private Button mAddNewPres;
	private LinearLayout mCurrPres;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_prescription);
		// Show the Up button in the action bar.
		
		//mManagePres = (Button) findViewById(R.id.a_pres);
		
		/*mManagePres.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.d(TAG, "manage prescription button clicked");
		}
		});*/
		
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
	
	private void addCurrentPrescriptions() {
		PrescriptionOpenHelper helper = new PrescriptionOpenHelper(this);
		List<String> names = helper.getBrandChemFilenames();
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		for (int i = 0; i < names.size(); i = i + 3) {
			Button presButton = new Button(ManagePrescriptionActivity.this);
			presButton.setLayoutParams(lp);
			String filename = names.get(i + 2);
			Bitmap bitmap = decodeSampledBitmapFromResource(filename, 200, 200);
			Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 200, 200, true));
			presButton.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
			String brandName = names.get(i);
			String chemName = names.get(i + 1);
			//presButton.setCompoundDrawables(img, null, null, null);
			presButton.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			presButton.setText(brandName + " (" + chemName + ")");
			presButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d(TAG, "Adding prescription button.");
					startActivity(new Intent(ManagePrescriptionActivity.this, EditPrescription.class));
				}
			});
			mCurrPres.addView(presButton);
		}
			
	}
	
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
}
