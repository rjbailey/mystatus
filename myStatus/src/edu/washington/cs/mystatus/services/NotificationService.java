package edu.washington.cs.mystatus.services;

import edu.washington.cs.mystatus.R;
import edu.washington.cs.mystatus.activities.SurveysActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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

	private static final String TAG = "mystatus.NotificationService";

	private static final int NOTIFICATION_ID = 0;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.w(TAG, "Generating notification");
		Notification surveyNotification = createSurveyNotification();
		surveyNotification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(NOTIFICATION_ID, surveyNotification);

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
				.setContentIntent(surveyIntent);
		return nb.build();
	}
}
