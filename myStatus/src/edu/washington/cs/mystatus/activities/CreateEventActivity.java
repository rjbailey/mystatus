package edu.washington.cs.mystatus.activities;

import edu.washington.cs.mystatus.R;
import edu.washington.cs.mystatus.application.MyStatus;
import edu.washington.cs.mystatus.receivers.ScreenOnOffReceiver;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

public class CreateEventActivity extends Activity {

	private Button mStartTime, mEndTime, mDate, mCreateEvent;
	private EditText mActivityTitle;
	private ScreenOnOffReceiver screenReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.mystatus_create_event);
		
		setGlobalVariables();
		
		addListenersOnButtons();
		
		// adding screen on off receiver for turning off the screen correctly
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		screenReceiver = new ScreenOnOffReceiver();
		registerReceiver(screenReceiver, intentFilter);
	}
	
	private void addListenersOnButtons() {
		mDate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//showDatePickerDialog(v);
			}
		});
		
		mStartTime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//showTimePickerDialog(v);
			}
		});
		
		mEndTime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//showTimePickerDialog(v);
			}
		});
		
		mCreateEvent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
			}
		});
	}
	
	private void setGlobalVariables() {
		mActivityTitle = (EditText) findViewById(R.id.set_activity_title);
		mDate = (Button) findViewById(R.id.activity_date_button);
		mStartTime = (Button) findViewById(R.id.activity_time_start);
		mEndTime = (Button) findViewById(R.id.activity_time_end);
		mCreateEvent = (Button) findViewById(R.id.finalize_event);
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
	
	
	
	// Will be switched with the showDialog() deprecated method
	/*@SuppressLint("NewApi")
	private void showTimePickerDialog(View v) {
	    DialogFragment newFragment = new TimePickerFragment() {
	    	@Override
	    	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
	    		/*SharedPreferences prefs = getPreferences(MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putInt("hour_setting", hourOfDay);
				editor.putInt("minute_setting", minute);
				updateEvent(hourOfDay, minute);
				editor.commit();
	    	}
	    };
	    newFragment.show(getFragmentManager(), "timePicker");
	}
	
	@SuppressLint("NewApi")
	private void showDatePickerDialog(View v) {
		DialogFragment newFragment = new DatePickerFragment() {
			@Override
			public void onDateSet(DatePicker view, int year, int month, int day) {
				
			}
		};
		newFragment.show(getFragmentManager(), "datePicker");
	}*/
}
