package edu.washington.cs.mystatus;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;

public class CalendarCreator {

	private static final String ACCOUNT_NAME = "mystatus";
	
	@SuppressLint("NewApi")
	private static Uri calendarUri() {
		return CalendarContract.Calendars.CONTENT_URI
	            .buildUpon()
	            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
	            .appendQueryParameter(Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
	            .appendQueryParameter(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
	            .build();
	}
	
	private static ContentValues buildContentValues(Calendar calendar) {
	    //String dispName = calendar.getName();  //Calendar.getName() returns a String
	    //String intName = INT_NAME_PREFIX + dispName;
	    final ContentValues cv = new ContentValues();
	    cv.put(Calendars.ACCOUNT_NAME, ACCOUNT_NAME);
	    cv.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
	    cv.put(Calendars.NAME, "mystatus");
	    cv.put(Calendars.CALENDAR_DISPLAY_NAME, "myStatus");
	    cv.put(Calendars.OWNER_ACCOUNT, ACCOUNT_NAME);
	    cv.put(Calendars.VISIBLE, 1);
	    return cv;
	}
	
	public static void addCalendar(Calendar calendar, ContentResolver cr) {
	    if (calendar == null)
	        throw new IllegalArgumentException();
	    
	    final ContentValues cv = buildContentValues(calendar);

	    Uri calUri = calendarUri();
	    cr.insert(calUri, cv);
	}
	
}
