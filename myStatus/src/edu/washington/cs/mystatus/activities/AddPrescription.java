package edu.washington.cs.mystatus.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import edu.washington.cs.mystatus.R;
import edu.washington.cs.mystatus.database.PrescriptionOpenHelper;
import edu.washington.cs.mystatus.services.PrescriptionNotificationService;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.text.InputType;
import android.text.format.DateFormat;

/**
 * 
 * @author Emily Chien (eechien@cs.washington.edu)
 *
 */

public class AddPrescription extends Activity {

	protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;
	private final int TIME_PICKER_DIALOG_ID = 0;
	private final int BNAME_ENTRY_ERROR_DIALOG_ID = 1;
	private final int CNAME_ENTRY_ERROR_DIALOG_ID = 2;
	private final int QUANTITY_ENTRY_ERROR_DIALOG_ID = 3;
	private final int NOTIFICATION_ID = 0;
	private final int DEFAULT_HOUR = 11;
	private final int DEFAULT_MINUTE = 30;
	private final String TAG = "mystatus.EditPrescriptionActivity";
	
	private EditText mBrandName;
	private EditText mChemName;
	private Button mAddTime;
	private Button mDeleteTime;
	private Button mAddPic;
	private Button mCancel;
	private Button mSave;
	private LinearLayout mPresTimeQuant;
	
	private List<LinearLayout> mQuantLayoutList;
	private List<LinearLayout> mTimeLayoutList;
	private List<EditText> mQuantTextList;
	private List<Button> mTimeButtonList;
	private int[] hours;
	private int[] mins;
	private double[] quants;
	private int currCount;
	
	private File mFile;
	private File mDir;
	
	// creates a Time Picker Dialog for time buttons that get added by user
	private TimePickerDialog.OnTimeSetListener mAddedTimeSetListeners =
			new TimePickerDialog.OnTimeSetListener() {
				@Override
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					view.setId(currCount - 1);
					if (view.getId() > hours.length) {
						hours = resizeArrayI(hours);
						mins = resizeArrayI(mins);
					}
					hours[view.getId()] = hourOfDay;
					mins[view.getId()] = minute;
					Button b = mTimeButtonList.get(view.getId());
					if (minute < 10) {
						b.setText(hourOfDay + ":0" + minute);
					} else {
						b.setText(hourOfDay + ":" + minute);
					}
				}
			};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_prescription);
		if (savedInstanceState != null) {
			String filename = savedInstanceState.getString("mFile");
			//String filename = savedInstanceState.getString("mFile", "");
			if (!filename.equals("")) {
				mFile = new File(filename);
			}
		}
		createDirForPics();
		
		setGlobalVars();
		addListeners();
		
		
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		//TODO check correct
		super.onSaveInstanceState(outState);
		outState.putString("mFile", mFile.getAbsolutePath());
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mFile = new File(savedInstanceState.getString("mFile"));
	}
	
	// set the views and lists
	private void setGlobalVars() {
		mBrandName = (EditText) findViewById(R.id.pres_brandname);
		mChemName = (EditText) findViewById(R.id.pres_chemname);
		mDeleteTime = (Button) findViewById(R.id.delete_pres_time);
		mDeleteTime.setVisibility(Button.INVISIBLE);
		mDeleteTime.setClickable(false);
		mAddTime = (Button) findViewById(R.id.add_pres_time);
		mAddPic = (Button) findViewById(R.id.add_pres_pic);
		mCancel = (Button) findViewById(R.id.edit_pres_cancel);
		mSave = (Button) findViewById(R.id.edit_pres_save);
		mPresTimeQuant = (LinearLayout) findViewById(R.id.pres_quant_times);
		mQuantLayoutList = new ArrayList<LinearLayout>();
		mTimeLayoutList = new ArrayList<LinearLayout>();
		mQuantTextList = new ArrayList<EditText>();
		mTimeButtonList = new ArrayList<Button>();
		hours = new int[10];
		mins = new int[10];
		quants = new double[10];
		for (int i = 0; i < 10; i++) {
			hours[i] = -1;
			mins[i] = -1;
			quants[i] = -1.0;
		}
	}
	
	private void addListeners() {
		
		// listener for button to delete new notification slots
		mDeleteTime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				currCount--;
				
				mQuantTextList.remove(currCount);
				mTimeButtonList.remove(currCount);
				
				mPresTimeQuant.removeView(mTimeLayoutList.get(currCount));
				mTimeLayoutList.remove(currCount);
				
				mPresTimeQuant.removeView(mQuantLayoutList.get(currCount));
				mQuantLayoutList.remove(currCount);
				
				if (currCount == 0) {
					mDeleteTime.setVisibility(Button.INVISIBLE);
					mDeleteTime.setClickable(false);
				}
			}
		});
		
		// listener for button to make new notification slots
		mAddTime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// make user fill out previous quant and time before they can
				// add another
				if (currCount != 0 &&
						(mQuantTextList.get(currCount - 1).getText().toString().equals("") ||
						hours[currCount - 1] == -1)) {
					Toast.makeText(AddPrescription.this,
							"Please fill out the previous Quantity and Time fields",
							Toast.LENGTH_LONG).show();
				// can only add times if picture taken so that mFile doesn't go away
				// TODO find alternative
				} else if (mFile == null) {
					Toast.makeText(AddPrescription.this, "Please take a picture first.",
							Toast.LENGTH_LONG).show();
				} else {
					if (currCount == 0) {
						createDeleteButton();
					}
					
					// create name layout
					LinearLayout quantLayout = new LinearLayout(AddPrescription.this);
					LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					quantLayout.setLayoutParams(lp);
					quantLayout.setOrientation(LinearLayout.HORIZONTAL);
					
					// create quant layout
					TextView quantTextView = new TextView(AddPrescription.this, null,
							android.R.attr.textAppearanceMedium);
					quantTextView.setLayoutParams(lp);
					quantTextView.setText(R.string.pres_quantity);
					quantLayout.addView(quantTextView);
					
					EditText quantEditText = new EditText(AddPrescription.this);
					LayoutParams quantEditTextLP = new LayoutParams(200, LayoutParams.WRAP_CONTENT);
					quantEditText.setLayoutParams(quantEditTextLP);
					quantEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
					quantEditText.setId(currCount);
					quantLayout.addView(quantEditText);
					
					TextView mgTextView = new TextView(AddPrescription.this, null,
							android.R.attr.textAppearanceMedium);
					mgTextView.setLayoutParams(lp);
					mgTextView.setText(R.string.pres_mg);
					quantLayout.addView(mgTextView);
					
					//create time layout
					LinearLayout timeLayout = new LinearLayout(AddPrescription.this);
					timeLayout.setLayoutParams(lp);
					timeLayout.setOrientation(LinearLayout.HORIZONTAL);
					
					TextView timeTextView = new TextView(AddPrescription.this, null,
							android.R.attr.textAppearanceMedium);
					timeTextView.setLayoutParams(lp);
					timeTextView.setText(R.string.pres_time);
					timeLayout.addView(timeTextView);
					
					Button timeButton = new Button(AddPrescription.this);
					timeButton.setLayoutParams(lp);
					timeButton.setText(DEFAULT_HOUR + ":" + DEFAULT_MINUTE);
					timeButton.setId(currCount);
					timeButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View view) {
							//view.setId(mQuantLayoutList.size());
							showDialog(TIME_PICKER_DIALOG_ID);
						}
					});
					timeLayout.addView(timeButton);
					
					//add name and time to mPresTimeQuant
					mQuantTextList.add(quantEditText);
					mTimeButtonList.add(timeButton);
					mQuantLayoutList.add(quantLayout);
					mTimeLayoutList.add(timeLayout);
					mPresTimeQuant.addView(quantLayout);
					mPresTimeQuant.addView(timeLayout);
					currCount++;
				}
			}
		});
		
		// listener to start camera activity
		mAddPic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// only allowed to take picture if brand name is filled out
				// because image name needs brand name
				if (mBrandName.getText().toString().equals("")) {
					Toast.makeText(AddPrescription.this,
							"Please fill out the Brand Name field",
							Toast.LENGTH_LONG).show();
				} else {
					String filename = mBrandName.getText().toString() + "_pic.jpg";
					File f = new File(mDir, filename);
				
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
					intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
					mFile = f;
					startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
				}
			}
		});
		
		// listener to get out of this page and save nothing
		mCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// save nothing
				finish();
			}
		});
		
		// listener to get out of this page and save the information
		// to the database and create notifications
		mSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String brandName = mBrandName.getText().toString();
				String chemName = mChemName.getText().toString();
				if (brandName.equals("")) {
					showDialog(BNAME_ENTRY_ERROR_DIALOG_ID);
				} else if (chemName.equals("")) {
					showDialog(CNAME_ENTRY_ERROR_DIALOG_ID);
				} else {
					
					// TODO: add a check that all times and quantities are filled
					
					Date today = new Date();
					today.setSeconds(0);
					enableNotifications(today.getTime(), NOTIFICATION_ID);
					for (int i = 0; i < currCount; i++) {
						if (currCount > quants.length) {
							resizeArrayD(quants);
						}
						quants[i] = Double.parseDouble(mQuantTextList.get(i).getText().toString());
						today.setHours(hours[i]);
						today.setMinutes(mins[i]);
						enableNotifications(today.getTime(), NOTIFICATION_ID);
						saveToDatabase(quants[i], hours[i], mins[i]);
					}
					finish();
				}
			}
		});
	}
	
	// notify user of whether the camera successfully or unsuccessfully took a picture
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			// TODO: decide if this can be used
			if (resultCode == RESULT_OK) {
				/*Toast.makeText(AddPrescription.this, "Image saved to:\n" +
	                     data.getData(), Toast.LENGTH_LONG).show();*/
				super.onActivityResult(requestCode, resultCode, data);
			} else if (resultCode == RESULT_CANCELED) {
				// user canceled image capture
			} else {
				// Image capture failed
				/*Toast.makeText(AddPrescription.this,
						"Somthing went wrong while taking a picture. Please try again.",
						Toast.LENGTH_LONG).show();*/
			}
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		// notifies user that the brand name was not filled out
		case BNAME_ENTRY_ERROR_DIALOG_ID:
			AlertDialog.Builder builderBN = new AlertDialog.Builder(this);
			builderBN.setMessage(R.string.dialog_bname_warning)
				.setPositiveButton(R.string.fire, new DialogInterface.OnClickListener() {		
					@Override
					public void onClick(DialogInterface dialog, int which) {}
				});
			return builderBN.create();
		// notifies user that the chemical name was not filled out
		case CNAME_ENTRY_ERROR_DIALOG_ID:
			AlertDialog.Builder builderCN = new AlertDialog.Builder(this);
			builderCN.setMessage(R.string.dialog_cname_warning)
				.setPositiveButton(R.string.fire, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {}
				});
			return builderCN.create();
		// notifies user that the quantity was not filled out
		case QUANTITY_ENTRY_ERROR_DIALOG_ID:
			AlertDialog.Builder builderQ = new AlertDialog.Builder(this);
			builderQ.setMessage(R.string.dialog_quantity_warning)
				.setPositiveButton(R.string.fire, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {}
				});
			return builderQ.create();
		case TIME_PICKER_DIALOG_ID:
			Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);
			return new TimePickerDialog(this, mAddedTimeSetListeners, hour, minute,
					DateFormat.is24HourFormat(this));
		}
		return null;
	}
	
	// Enables an AlarmManager to create an alarm when its time for the calendar event
	private void enableNotifications(long startTime, int id) {
		PendingIntent notifyIntent = PendingIntent.getService(this, id,
				new Intent(this, PrescriptionNotificationService.class), 0);
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, startTime, 86400000, notifyIntent);
		Log.i(TAG, "Enabled notifications");
	}
	
	// to appear when a new layout is added to the page
	private void createDeleteButton() {
		mDeleteTime.setVisibility(Button.VISIBLE);
		mDeleteTime.setClickable(true);
	}
	
	// create the directory for Pictures onto SD card
	private void createDirForPics() {
		String dirname = Environment.getExternalStorageDirectory() + "/mystatus";
		mDir = new File(dirname, "Pictures");
		if (!mDir.exists()) {
			mDir.mkdirs();
		}
	}
	
	// save the information to the database
	private void saveToDatabase(double quant, int hour, int min) {
		//Uri presUri;
		/*ContentValues values = new ContentValues();
		values.put(PrescriptionOpenHelper.BRAND_NAME, mBrandName.getText().toString());
		values.put(PrescriptionOpenHelper.CHEMICAL_NAME, mChemName.getText().toString());
		values.put(PrescriptionOpenHelper.QUANTITY, quant);
		values.put(PrescriptionOpenHelper.HOUR, hour);
		values.put(PrescriptionOpenHelper.MINUTE, min);
		String filename = mFile.getAbsolutePath();
		values.put(PrescriptionOpenHelper.PICTURE_FILENAME, filename);
		// TODO: insert _ID
		PrescriptionOpenHelper helper = new PrescriptionOpenHelper(this);
		SQLiteDatabase db = helper.getWritableDatabase();
		db.insert("prescriptions.db", null, values);
		//presUri = db.insert(PrescriptionColumns.CONTENT_URI, values);*/
		String brandName = mBrandName.getText().toString();
		String chemName = mChemName.getText().toString();
		String filename = mFile.getAbsolutePath();
		PrescriptionOpenHelper helper = new PrescriptionOpenHelper(this);
		helper.addNewPrescriptionNotification(brandName, chemName, filename, quant, hour, min);
		int count = helper.getTotalCount();
	}

	private int[] resizeArrayI(int[] arr) {
		int[] newArr = new int[arr.length * 2];
		for (int i = 0; i < arr.length; i++) {
			newArr[i] = arr[i];
		}
		for (int i = arr.length; i < newArr.length; i++) {
			newArr[i] = -1;
		}
		return newArr;
	}
	
	private double[] resizeArrayD(double[] arr) {
		double[] newArr = new double[arr.length * 2];
		for (int i = 0; i < arr.length; i++) {
			newArr[i] = arr[i];
		}
		for (int i = arr.length; i < newArr.length; i++) {
			newArr[i] = -1.0;
		}
		return newArr;
	}
}
