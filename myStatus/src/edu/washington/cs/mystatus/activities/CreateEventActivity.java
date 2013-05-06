package edu.washington.cs.mystatus.activities;

import edu.washington.cs.mystatus.R;
import edu.washington.cs.mystatus.fragments.DatePickerFragment;
import edu.washington.cs.mystatus.fragments.TimePickerFragment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_create_event);
		
		setGlobalVariables();
		
		addListenersOnButtons();
	}
	
	private void addListenersOnButtons() {
		mDate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDatePickerDialog(v);
			}
		});
		
		mStartTime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showTimePickerDialog(v);
			}
		});
		
		mEndTime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showTimePickerDialog(v);
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
	
	@SuppressLint("NewApi")
	private void showTimePickerDialog(View v) {
	    DialogFragment newFragment = new TimePickerFragment() {
	    	@Override
	    	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
	    		/*SharedPreferences prefs = getPreferences(MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putInt("hour_setting", hourOfDay);
				editor.putInt("minute_setting", minute);
				updateEvent(hourOfDay, minute);
				editor.commit();*/
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
	}
}
