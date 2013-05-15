package edu.washington.cs.mystatus.activities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import edu.washington.cs.mystatus.R;
import edu.washington.cs.mystatus.services.PrescriptionNotificationService;
import android.net.Uri;
import android.os.Bundle;
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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.text.InputType;
import android.text.format.DateFormat;

/**
 * 
 * @author Emily Chien (eechien@cs.washington.edu)
 *
 */

public class EditPrescription extends Activity {

	//TODO set this
	protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;
	private final int TIME_PICKER_DIALOG_ID = 0;
	private final int NAME_ENTRY_ERROR_DIALOG_ID = 1;
	private final int QUANTITY_ENTRY_ERROR_DIALOG_ID = 2;
	private final int NOTIFICATION_ID = 0;
	private final int DEFAULT_HOUR = 11;
	private final int DEFAULT_MINUTE = 30;
	private final String TAG = "mystatus.EditPrescriptionActivity";
	
	private EditText mName;
	private EditText mQuantity;
	private Button mTime; // to timepicker
	private Button mAddTime;
	private Button mAddPic;
	private Button mCancel;
	private Button mSave;
	private LinearLayout mPresLayout;
	private LinearLayout mPresTimeQuant;
	
	private Button mDeleteTime;
	private List<LinearLayout> mQuantLayoutList;
	private List<LinearLayout> mTimeLayoutList;
	
	private int mHour;
	private int mMin;
	
	
	private TimePickerDialog.OnTimeSetListener mTimeSetListener =
			new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			mHour = hourOfDay;
			mMin = minute;
			mTime.setText(mHour + ":" + mMin);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_prescription);
		
		setGlobalVars();
		addListeners();
	}
	
	private void setGlobalVars() {
		mName = (EditText) findViewById(R.id.pres_name);
		mQuantity = (EditText) findViewById(R.id.pres_quantity);
		mTime = (Button) findViewById(R.id.pres_time);
		mTime.setText(DEFAULT_HOUR + ":" + DEFAULT_MINUTE);
		mDeleteTime = (Button) findViewById(R.id.delete_pres_time);
		mDeleteTime.setVisibility(Button.INVISIBLE);
		mDeleteTime.setClickable(false);
		mAddTime = (Button) findViewById(R.id.add_pres_time);
		mAddPic = (Button) findViewById(R.id.add_pres_pic);
		mCancel = (Button) findViewById(R.id.edit_pres_cancel);
		mSave = (Button) findViewById(R.id.edit_pres_save);
		mPresTimeQuant = (LinearLayout) findViewById(R.id.pres_quant_times);
		mPresLayout = (LinearLayout) findViewById(R.id.pres_layout);
		mQuantLayoutList = new ArrayList<LinearLayout>();
		mTimeLayoutList = new ArrayList<LinearLayout>();
	}
	
	private void addListeners() {
		mTime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(TIME_PICKER_DIALOG_ID);
			}
		});
		
		mDeleteTime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int timeListLastIndex = mTimeLayoutList.size() - 1;
				mPresTimeQuant.removeView(mTimeLayoutList.get(timeListLastIndex));
				mTimeLayoutList.remove(timeListLastIndex);
				int quantListLastIndex = mQuantLayoutList.size() - 1;
				mPresTimeQuant.removeView(mQuantLayoutList.get(quantListLastIndex));
				mQuantLayoutList.remove(quantListLastIndex);
				
				if (mTimeLayoutList.size() == 0) {
					mDeleteTime.setVisibility(Button.INVISIBLE);
					mDeleteTime.setClickable(false);
				}
			}
		});
		
		mAddTime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if (mQuantLayoutList.size() == 0) {
					createDeleteButton();
				}
				
				// create name layout
				LinearLayout quantLayout = new LinearLayout(EditPrescription.this);
				LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				quantLayout.setLayoutParams(lp);
				quantLayout.setOrientation(LinearLayout.HORIZONTAL);
				
				TextView quantTextView = new TextView(EditPrescription.this, null,
						android.R.attr.textAppearanceMedium);
				quantTextView.setLayoutParams(lp);
				quantTextView.setText(R.string.pres_quantity);
				quantLayout.addView(quantTextView);
				
				EditText quantEditText = new EditText(EditPrescription.this);
				LayoutParams quantEditTextLP = new LayoutParams(200, LayoutParams.WRAP_CONTENT);
				quantEditText.setLayoutParams(quantEditTextLP); // TODO
				quantEditText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
				quantLayout.addView(quantEditText);
				
				TextView mgTextView = new TextView(EditPrescription.this, null,
						android.R.attr.textAppearanceMedium);
				mgTextView.setLayoutParams(lp);
				mgTextView.setText(R.string.pres_mg);
				quantLayout.addView(mgTextView);
				
				//create time layout
				LinearLayout timeLayout = new LinearLayout(EditPrescription.this);
				timeLayout.setLayoutParams(lp);
				timeLayout.setOrientation(LinearLayout.HORIZONTAL);
				
				TextView timeTextView = new TextView(EditPrescription.this, null,
						android.R.attr.textAppearanceMedium);
				timeTextView.setLayoutParams(lp);
				timeTextView.setText(R.string.pres_time);
				timeLayout.addView(timeTextView);
				
				Button timeButton = new Button(EditPrescription.this);
				timeButton.setLayoutParams(lp);
				timeButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						showDialog(TIME_PICKER_DIALOG_ID);
					}
				});
				timeButton.setText(DEFAULT_HOUR + ":" + DEFAULT_MINUTE);
				timeLayout.addView(timeButton);
				
				//add name and time to mPresTimeQuant
				mQuantLayoutList.add(quantLayout);
				mTimeLayoutList.add(timeLayout);
				mPresTimeQuant.addView(quantLayout);
				mPresTimeQuant.addView(timeLayout);
			}
		});
		
		mAddPic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO
				// go to camera
				String filename = "pres_pic.jpg";
				ContentValues values = new ContentValues();
				values.put(MediaStore.Images.Media.TITLE, filename);
				Uri imageUri =
						getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
				intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
				startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
				// take pic
				// save to database
			}
		});
		
		mCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// save nothing
				finish();
			}
		});
		
		mSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO save things to database
				String name = mName.getText().toString();
				String quantityS = mQuantity.getText().toString();
				if (name.equals("")) {
					showDialog(NAME_ENTRY_ERROR_DIALOG_ID);
					
				} else if (quantityS.equals("")) {
					// TODO create notification
					showDialog(QUANTITY_ENTRY_ERROR_DIALOG_ID);
				} else {
					double quantityD = Double.parseDouble(quantityS);
					//int hour = mHour;
					//int minute = mMin;
					
					Date today = new Date();
					today.setHours(mHour);
					today.setMinutes(mMin);
					today.setSeconds(0);
					enableNotifications(today.getTime(), NOTIFICATION_ID);
					
					finish();
				}
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// save to database
			} else if (resultCode == RESULT_CANCELED) {
				
			} else {
				
			}
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case TIME_PICKER_DIALOG_ID:
			Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);
			// TODO always changes the first buttons text
			return new TimePickerDialog(this, mTimeSetListener, hour, minute, DateFormat.is24HourFormat(this));
		case NAME_ENTRY_ERROR_DIALOG_ID:
			AlertDialog.Builder builderN = new AlertDialog.Builder(this);
			builderN.setMessage(R.string.dialog_name_warning)
				.setPositiveButton(R.string.fire, new DialogInterface.OnClickListener() {		
					@Override
					public void onClick(DialogInterface dialog, int which) {}
				});
			return builderN.create();
		case QUANTITY_ENTRY_ERROR_DIALOG_ID:
			AlertDialog.Builder builderQ = new AlertDialog.Builder(this);
			builderQ.setMessage(R.string.dialog_quantity_warning)
				.setPositiveButton(R.string.fire, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {}
				});
			return builderQ.create();
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
	
	private void createDeleteButton() {
		mDeleteTime.setVisibility(Button.VISIBLE);
		mDeleteTime.setClickable(true);
	}
}
