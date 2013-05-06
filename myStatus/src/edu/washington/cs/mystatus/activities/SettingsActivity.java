package edu.washington.cs.mystatus.activities;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import edu.washington.cs.mystatus.R;
import edu.washington.cs.mystatus.fragments.TimePickerFragment;
import edu.washington.cs.mystatus.services.NotificationService;
import edu.washington.cs.mystatus.CalendarCreator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Spinner;

/**
 * SettingsActivity provides a UI for managing notification settings.
 * 
 * It is also responsible for setting up recurring notifications when
 * notifications are enabled, and cleaning up when notifications are disabled.
 * 
 * @author Emily Chien (eechien@cs.washington.edu)
 * @see AlarmManager
 * @see NotificationService
 */
@SuppressLint("NewApi")
public class SettingsActivity extends Activity {
	
	private static final String TAG = "mystatus.SettingsActivity";
	
	private static final String DEFAULT_DAY = "Sunday";
	private static final int DEFAULT_HOUR = 17;
	private static final int DEFAULT_MIN = 30;
	private static long EVENT_ID;
	private static long CALENDAR_ID;
	
	//private CheckBox mEnableNotificationsBox;
	private Spinner mDay;
	private Button mDayButton;
	private Button mTime;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		Calendar cal = Calendar.getInstance();
		ContentResolver cr = getContentResolver();
		CalendarCreator.addCalendar(cal, cr);
		
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		String notDefaultDay = settings.getString("day_setting", DEFAULT_DAY);
		int notDefaultDayPosition = settings.getInt("day_position_setting", 0);
		int notDefaultHour = settings.getInt("hour_setting", DEFAULT_HOUR);
		int notDefaultMinute = settings.getInt("minute_setting", DEFAULT_MIN);
		EVENT_ID = settings.getLong("event_id", -1);
		CALENDAR_ID = getCalendarId();
		if (CALENDAR_ID == -1) {
			CALENDAR_ID = 1;
		}
		
		mDay = (Spinner) findViewById(R.id.day);
		mDayButton = (Button) findViewById(R.id.day_button);
		
		mDayButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences prefs = getPreferences(MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				
				editor.putString("day_setting", String.valueOf(mDay.getSelectedItem()));
				editor.putInt("day_position_setting", mDay.getSelectedItemPosition());
				updateEvent(String.valueOf(mDay.getSelectedItem()));
				editor.commit();
			}	
		});
		
		mDay.setSelection(notDefaultDayPosition);

		mTime = (Button) findViewById(R.id.time);
		mTime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showTimePickerDialog(v);
			}
		});
		
		// TODO: make only appear once, edit from then on
		//if (EVENT_ID == -1) {
			createEvent(notDefaultDay, notDefaultHour, notDefaultMinute);
		//}
	}
	
	private void createEvent(String day, int hour, int min) {
		// create beginning of event and end of event
		Date beginTime = new Date();
		beginTime.setHours(hour);
		beginTime.setMinutes(min);
		Date  endTime = new Date();
		endTime.setHours(hour + 1);
		endTime.setMinutes(min);
		Calendar getTimeZone = Calendar.getInstance();
		TimeZone tz = getTimeZone.getTimeZone();
		// create the event
		ContentResolver cr = getContentResolver();
		ContentValues values = new ContentValues();
		values.put(Events.DTSTART, beginTime.getTime());
		values.put(Events.DTEND, endTime.getTime());
		values.put(Events.TITLE, "myStatus");
		values.put(Events.CALENDAR_ID, CALENDAR_ID); // TODO: make own calendar
		values.put(Events.EVENT_TIMEZONE, tz.getDisplayName());
		values.put(Events.RRULE, "FREQ=WEEKLY;BYDAY=" + getWeekdayAbr(day));
		
		// add the event
		Uri uri = cr.insert(Events.CONTENT_URI, values);
		EVENT_ID = Long.parseLong(uri.getLastPathSegment());
		Log.i(TAG, "Created event.");
		
		// adds reminder in the form of an alert
		ContentValues reminders = new ContentValues();
		reminders.put(Reminders.EVENT_ID, EVENT_ID);
		reminders.put(Reminders.METHOD, Reminders.METHOD_ALERT);
		reminders.put(Reminders.MINUTES, 0);
		cr.insert(Reminders.CONTENT_URI, reminders);
		Log.i(TAG, "Created event alert.");
		// TODO: customize the reminder
		
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong("event_id", EVENT_ID);
		editor.commit();
	}
	
	private void updateEvent(String newDay) {
		ContentResolver cr = getContentResolver();
		ContentValues values = new ContentValues();
		Uri updateUri = null;
		
		values.put(Events.RRULE, "FREQ=WEEKLY;BYDAY=" + getWeekdayAbr(newDay));
		
		updateUri = ContentUris.withAppendedId(Events.CONTENT_URI, EVENT_ID);
		int rows = cr.update(updateUri, values, null, null);
		Log.i(TAG, "Rows updated: " + rows);
	}
	
	private void updateEvent(int newHour, int newMin) {
		ContentResolver cr = getContentResolver();
		ContentValues values = new ContentValues();
		Uri updateUri = null;
		
		Date beginTime = new Date();
		beginTime.setHours(newHour);
		beginTime.setMinutes(newMin);
		values.put(Events.DTSTART, beginTime.getTime());
		Date endTime = new Date();
		endTime.setHours(newHour + 1);
		endTime.setMinutes(newMin);
		values.put(Events.DTEND, endTime.getTime());
		
		updateUri = ContentUris.withAppendedId(Events.CONTENT_URI, EVENT_ID);
		int rows = cr.update(updateUri, values, null, null);
		Log.i(TAG, "Rows updated: " + rows);
	}
	
	private void showTimePickerDialog(View v) {
	    DialogFragment newFragment = new TimePickerFragment() {
	    	@Override
	    	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
	    		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putInt("hour_setting", hourOfDay);
				editor.putInt("minute_setting", minute);
				updateEvent(hourOfDay, minute);
				editor.commit();
	    	}
	    };
	    newFragment.show(getFragmentManager(), "timePicker");
	}
	
	// Returns the 2 letter capital abbreviation of the
	// weekday passed
	private String getWeekdayAbr(String weekday) {
		return weekday.substring(0, 2).toUpperCase();
	}
	
	// returns true when the "myStatus" calendar id is found
	// false otherwise
	private int getCalendarId() {
		ContentResolver cr = getContentResolver();
		Cursor cursor = cr.query(Uri.parse("content://com.android.calendar/calendars"),
		        (new String[] { "_id", "calendar_displayName" }), null, null, null);
		
		while (cursor.moveToNext()) {
			String displayName = cursor.getString(1);
			if (displayName.equals("myStatus")) {
				return Integer.parseInt(cursor.getString(0));
			}
		}
		return -1;
	}
}
