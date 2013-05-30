package edu.washington.cs.mystatus.services;

import edu.washington.cs.mystatus.R;
import edu.washington.cs.mystatus.activities.SurveyListTabs;
import edu.washington.cs.mystatus.application.MyStatus;
import edu.washington.cs.mystatus.providers.FormsProviderAPI.FormTypes;
import edu.washington.cs.mystatus.providers.FormsProviderAPI.FormsColumns;
import edu.washington.cs.mystatus.utilities.PredicateSolver;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
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
public class NotificationService extends Service {

	private static final String TAG = "NotificationService";

	private static final int NOTIFICATION_ID = 0;

	private static final String ACTION_DISMISS = "dismiss";
	private static final String ACTION_SNOOZE = "snooze";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "NotificationService started.");

		String action = intent.getAction();
		if (action == null) {
			Log.d(TAG, "Notifying if needed.");
			notifyIfNeeded();
		} else if (action.equals(ACTION_DISMISS)) {
			Log.d(TAG, "Dismissing notification.");
			NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(NOTIFICATION_ID);
		} else if (action.equals(ACTION_SNOOZE)) {
			// TODO: Support snooze for notifications
			Log.d(TAG, "Snooze requested--dismissing notification instead.");
			NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(NOTIFICATION_ID);
		}

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
			Notification surveyNotification = createSurveyNotification();
			surveyNotification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
			NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			nm.notify(NOTIFICATION_ID, surveyNotification);
		} else {
			Log.i(TAG, "No surveys need a response, so no notification was generated.");
		}
		c.close();
	}

	/**
	 * @return A Notification which launches the surveys list when clicked.
	 */
	private Notification createSurveyNotification() {
		// Create a PendingIntent that will launch an ODK Collect survey
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

		NotificationCompat.Builder nb = new NotificationCompat.Builder(this)
				.setSmallIcon(android.R.drawable.ic_menu_my_calendar)
				.setContentTitle(getResources().getText(R.string.notification_title))
				.setContentText(getResources().getText(R.string.notification_message))
				.setContentIntent(surveyIntent)				
				.setAutoCancel(true)

				.setStyle(new NotificationCompat.BigTextStyle()
						.bigText(getResources().getText(R.string.notification_message)))
				.addAction(android.R.drawable.ic_menu_close_clear_cancel,
						getString(R.string.notification_dismiss), piDismiss)
				.addAction(android.R.drawable.ic_menu_recent_history,
						getString(R.string.notification_snooze), piSnooze);
				
		return nb.build();
	}
}
