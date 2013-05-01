package edu.washington.cs.mystatus.services;

import edu.washington.cs.mystatus.R;
import edu.washington.cs.mystatus.activities.SurveysActivity;
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
 */
public class NotificationService extends Service {

	private static final String TAG = "NotificationService";

	private static final int NOTIFICATION_ID = 0;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "NotificationService started.");

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
			Log.i(TAG, "Generating notification");
			Notification surveyNotification = createSurveyNotification();
			NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			nm.notify(NOTIFICATION_ID, surveyNotification);
		} else {
			Log.i(TAG, "No surveys need a response, so no notification was generated.");
		}

		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * @return A Notification which launches the surveys list when clicked.
	 */
	private Notification createSurveyNotification() {
		// Create a PendingIntent that will launch an ODK Collect survey
		PendingIntent surveyIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, SurveysActivity.class),
				Intent.FLAG_ACTIVITY_NEW_TASK);

		NotificationCompat.Builder nb = new NotificationCompat.Builder(this)
				.setSmallIcon(android.R.drawable.ic_menu_my_calendar)
				.setContentTitle(getResources().getText(R.string.notification_title))
				.setContentText(getResources().getText(R.string.notification_message))
				.setContentIntent(surveyIntent)
				.setAutoCancel(true)
				.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
				
		return nb.build();
	}
}
