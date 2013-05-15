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
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Events;
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
	private static final int DEFAULT_HOUR = 8;
	private static final int DEFAULT_MIN = 30;
	private static String DAY;
	private static int HOUR;
	private static int MINUTE;
	private static long EVENT_ID;
	private static long CALENDAR_ID;
	
	//private CheckBox mEnableNotificationsBox;
	private Spinner mDay;
	private Button mDayButton;
	private Button mTime;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mystatus_settings);

		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		DAY = settings.getString("day_setting", DEFAULT_DAY);
		int notDefaultDayPosition = settings.getInt("day_position_setting", 4);
		HOUR = settings.getInt("hour_setting", DEFAULT_HOUR);
		MINUTE = settings.getInt("minute_setting", DEFAULT_MIN);
		EVENT_ID = settings.getLong("event_id", -1);
		CALENDAR_ID = settings.getLong("calendar_id", -1);
		if (CALENDAR_ID == -1) {
			Calendar cal = Calendar.getInstance();
			ContentResolver cr = getContentResolver();
			CalendarCreator.addCalendar(cal, cr);
			CALENDAR_ID = getCalendarId();
		}
		
		mDay = (Spinner) findViewById(R.id.day);
		mDayButton = (Button) findViewById(R.id.day_button);
		
		mDayButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences prefs = getPreferences(MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				
				DAY = String.valueOf(mDay.getSelectedItem());
				editor.putString("day_setting", String.valueOf(mDay.getSelectedItem()));
				editor.putInt("day_position_setting", mDay.getSelectedItemPosition());
				String day = String.valueOf(mDay.getSelectedItem());
				updateEvent(HOUR, MINUTE, day);
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
		if (EVENT_ID == -1) {
			createEvent(DAY, HOUR, MINUTE);
		}
	}
	
	// Creates a calendar event in the myStatus Calendar with the date and time provided
	// in the parameters
	private void createEvent(String day, int hour, int min) {
		// create beginning of event and end of event
		Date beginTime = new Date();
		beginTime.setDate(getDate(beginTime, day));
		beginTime.setHours(hour);
		beginTime.setMinutes(min);
		
		Date  endTime = new Date();
		endTime.setDate(getDate(endTime, day));
		endTime.setHours(hour + 1);
		endTime.setMinutes(min);
		Calendar cal = Calendar.getInstance();
		TimeZone tz = cal.getTimeZone();
		// create the event
		ContentResolver cr = getContentResolver();
		ContentValues values = new ContentValues();
		values.put(Events.DTSTART, beginTime.getTime());
		values.put(Events.DTEND, endTime.getTime());
		values.put(Events.TITLE, "myStatus");
		values.put(Events.CALENDAR_ID, CALENDAR_ID);
		values.put(Events.EVENT_TIMEZONE, tz.getDisplayName());
		values.put(Events.RRULE, "FREQ=WEEKLY;BYDAY=" + getWeekdayAbr(day));
		
		// add the event
		Uri uri = cr.insert(Events.CONTENT_URI, values);
		EVENT_ID = Long.parseLong(uri.getLastPathSegment());
		Log.i(TAG, "Created event.");
		
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong("event_id", EVENT_ID);
		editor.commit();
		
		long startTime = getEventStartTime();
		enableNotifications(startTime);
	}
	
	// Updates the date and time of the calendar event to the parameters given
	private void updateEvent(int hour, int min, String day) {
		disableNotifications();
		
		ContentResolver cr = getContentResolver();
		ContentValues values = new ContentValues();
		Uri updateUri = null;
		
		Date beginTime = new Date();
		beginTime.setHours(hour);
		beginTime.setMinutes(min);
		beginTime.setDate(getDate(beginTime, day));
		values.put(Events.DTSTART, beginTime.getTime());
		Date endTime = new Date();
		endTime.setHours(hour + 1);
		endTime.setMinutes(min);
		endTime.setDate(getDate(endTime, day));
		values.put(Events.DTEND, endTime.getTime());
		values.put(Events.RRULE, "FREQ=WEEKLY;BYDAY=" + getWeekdayAbr(day));

		int rows = cr.update(Events.CONTENT_URI, values, Events._ID + " =? ", new String[]{Long.toString(EVENT_ID)});
		Log.i(TAG, "Rows updated: " + rows);
		
		enableNotifications(getEventStartTime());
	}
	
	private void showTimePickerDialog(View v) {
	    DialogFragment newFragment = new TimePickerFragment() {
	    	@Override
	    	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
	    		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				HOUR = hourOfDay;
				MINUTE = minute;
				editor.putInt("hour_setting", hourOfDay);
				editor.putInt("minute_setting", minute);
				updateEvent(hourOfDay, minute, DAY);
				editor.commit();
	    	}
	    };
	    newFragment.show(getFragmentManager(), "timePicker");
	}
	
	// Returns the 2 letter capital abbreviation of the
	// weekday passed
	private String getWeekdayAbr(String weekday) {
		String ret = weekday.substring(0, 2).toUpperCase();
		return ret;
	}
	
	// gets the calendar ID for the myStatus calendar
	private long getCalendarId() {
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
	
	// Get the calendar event's start time in milliseconds
	private long getEventStartTime() {
		ContentResolver cr = getContentResolver();
		Cursor cursor = cr.query(Uri.parse("content://com.android.calendar/events"),
				(new String[] {"title", "dtstart"} ), "calendar_id=" + CALENDAR_ID, null, null);
		while(cursor.moveToNext()) {
			String eventName = cursor.getString(0);
			if (eventName.equals("myStatus")) {
				return Long.parseLong(cursor.getString(1));
			}
		}
		return 0;
		
	}
	
	// Enables an AlarmManager to create an alarm when its time for the calendar event
	private void enableNotifications(long startTime) {
		PendingIntent notifyIntent = PendingIntent.getService(this, 0,
				new Intent(this, NotificationService.class), 0);
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, startTime, 604800000, notifyIntent);
		Log.i(TAG, "Enabled notifications");
	}

	/**
	 * Cancels the AlarmManager alarm that generates notifications.
	 */
	private void disableNotifications() {
		Log.w(TAG, "Disabling notifications");
		PendingIntent notificationIntent = PendingIntent.getService(this, 0,
				new Intent(this, NotificationService.class), 0);
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		am.cancel(notificationIntent);
	}
	
	// get the day of the month for the next day of the week.
	private int getDate(Date today, String desiredDay) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		int todaysDate = cal.get(Calendar.DAY_OF_WEEK);
		int desiredDayNum = getDay(desiredDay);
		int diff = desiredDayNum - todaysDate;
		return cal.get(Calendar.DATE) + diff;	
	}
	
	// get the integer value from the String of a day of the week as
	// it appears for Calendar 
	private int getDay(String desiredDay) {
		if (desiredDay.equals("Monday"))
			return 1;
		else if (desiredDay.equals("Tuesday"))
			return 2;
		else if (desiredDay.equals("Wednesday"))
			return 3;
		else if (desiredDay.equals("Thursday"))
			return 4;
		else if (desiredDay.equals("Friday"))
			return 5;
		else if (desiredDay.equals("Saturday"))
			return 6;
		else
			return 7;
	}
}
