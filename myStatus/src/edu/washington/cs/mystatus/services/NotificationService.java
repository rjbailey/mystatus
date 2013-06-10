package edu.washington.cs.mystatus.services;

import info.guardianproject.cacheword.ICacheWordSubscriber;
import edu.washington.cs.mystatus.R;
import edu.washington.cs.mystatus.activities.SurveyListTabs;
import edu.washington.cs.mystatus.application.MyStatus;
import edu.washington.cs.mystatus.odk.provider.FormsProviderAPI.FormTypes;
import edu.washington.cs.mystatus.odk.provider.FormsProviderAPI.FormsColumns;
import edu.washington.cs.mystatus.utilities.PredicateSolver;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * NotificationService is responsible for generating survey reminder
 * notifications when appropriate.
 * 
 * @author Jake Bailey (rjacob@cs.washington.edu)
 * @author Chuong Dao (chuongd@cs.washington.edu)
 * @author Emily Chien (eechien@cs.washington.edu)
 */
public class NotificationService extends Service{

	private static final String TAG = "NotificationService";

	private static final int NOTIFICATION_ID = 0;
	private static final int NOTIFICATION_ID_SNOOZE = 1;
	private static int MILLIS = 300000; // 5 minutes
	
	private static final String ACTION_DISMISS = "dismiss";
	private static final String ACTION_SNOOZE = "snooze";
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "NotificationService started.");
		String action = intent.getAction();
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (action == null) {
			Log.d(TAG, "Notifying if needed.");
			notifyIfNeeded();
		} else if (action.equals(ACTION_DISMISS)) {
			Log.d(TAG, "Dismissing notification.");
			nm.cancel(NOTIFICATION_ID);
		} else if (action.equals(ACTION_SNOOZE)) {
			Log.d(TAG, "Snoozeing notification.");
			nm.cancel(NOTIFICATION_ID);
			startSnooze();
		}
		// fixed this for preventing services from restart
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * Generate a notification if at least one passive survey needs a response.
	 */
	public void notifyIfNeeded() {
		// mark all passive forms that satisfy their predicate as needing a response
		PredicateSolver.evaluateAllPredicates();

		// find all forms needing a response
		String selection = FormsColumns.NEEDS_RESPONSE + " = 1 AND "
				+ FormsColumns.FORM_TYPE + " = ?";
		String[] selectionArgs = { Integer.toString(FormTypes.PASSIVE) };

		Cursor c = MyStatus.getInstance().getContentResolver()
				.query(FormsColumns.CONTENT_URI, null, selection, selectionArgs, null);

		// only generate a notification if there's at least one survey to respond to
		if (c.getCount() > 0) {
			Log.i(TAG, c.getCount() + " survey(s) need a response. Generating notification...");
			issueNotification();
		} else {
			Log.i(TAG, "No surveys need a response, so no notification was generated.");
		}
		c.close();
	}
	
	// creates a notification with snooze and dismiss buttons
	private void issueNotification() {
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		PendingIntent surveyIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, SurveyListTabs.class), Intent.FLAG_ACTIVITY_NEW_TASK);
		
		NotificationCompat.BigTextStyle bigViewStyle = new NotificationCompat.BigTextStyle();
		// Sets up the Snooze and Dismiss action buttons that will appear in the
		// big view of the notification.
		Intent dismissIntent = new Intent(this, NotificationService.class);
		dismissIntent.setAction(ACTION_DISMISS);
		PendingIntent piDismiss = PendingIntent.getService(this, 0, dismissIntent, 0);

		Intent snoozeIntent = new Intent(this, NotificationService.class);
		snoozeIntent.setAction(ACTION_SNOOZE);
		PendingIntent piSnooze = PendingIntent.getService(this, 0, snoozeIntent, 0);

		builder = new NotificationCompat.Builder(this)
				.setSmallIcon(android.R.drawable.ic_menu_my_calendar)
				.setContentTitle(getResources().getText(R.string.notification_title))
				.setContentText(getResources().getText(R.string.notification_message))
				.setContentIntent(surveyIntent)
				.setDefaults(Notification.DEFAULT_ALL)
				.setAutoCancel(true)
				
				// for devices Android 4.1 and later
				.setStyle(bigViewStyle
						.bigText(getResources().getText(R.string.notification_message)))
				.addAction(R.drawable.remove,
						getString(R.string.notification_dismiss), piDismiss)
				.addAction(R.drawable.snooze,
						getString(R.string.notification_snooze), piSnooze);
		mNotificationManager.notify(NOTIFICATION_ID, builder.build());
	}
	
	// creates a notification after another 5 minutes
	private void startSnooze() {
		PendingIntent notifyIntent = PendingIntent.getService(this, NOTIFICATION_ID_SNOOZE,
				new Intent(this, NotificationService.class), 0);
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		long startTime = System.currentTimeMillis() + MILLIS;
		am.set(AlarmManager.RTC_WAKEUP, startTime, notifyIntent);
		Log.i(TAG, "Enabled notifications");
	}

}
