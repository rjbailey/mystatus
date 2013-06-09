/**
 * Creates a calendar on the Android Calendar called myStatus
 * so that events can be added to it specifically.
 * 
 * @author Emily Chien (eechien@cs.washington.edu)
 * 
 */

package edu.washington.cs.mystatus.utilities;

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
	    final ContentValues cv = new ContentValues();
	    cv.put(Calendars.ACCOUNT_NAME, ACCOUNT_NAME);
	    cv.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
	    cv.put(Calendars.NAME, "mystatus");
	    cv.put(Calendars.CALENDAR_DISPLAY_NAME, "myStatus");
	    cv.put(Calendars.OWNER_ACCOUNT, ACCOUNT_NAME);
	    cv.put(Calendars.VISIBLE, 1);
	    cv.put(Calendars.CALENDAR_TIME_ZONE, Calendar.getInstance().getTimeZone().getDisplayName());
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
