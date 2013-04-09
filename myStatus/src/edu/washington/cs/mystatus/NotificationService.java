package edu.washington.cs.mystatus;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class NotificationService extends Service {
	private final String TAG = "ODK_LAUNCHER";

	private static final int NOTIFICATION_ID = 0;

	private static final String ODK_COLLECT_PACKAGE = "org.odk.collect.android";
	private static final String ODK_COLLECT_CMP = "org.odk.collect.android.activities.FormEntryActivity";
	private static final String ODK_FORMS_URI = "content://org.odk.collect.android.provider.odk.forms/forms/1";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.w(TAG, "Generating notification");

		Notification collectNotification = createSurveyNotification();
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(NOTIFICATION_ID, collectNotification);

		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * Construct a PendingIntent to launch an ODK survey.
	 * 
	 * @return PendingIntent which launches the ODK FormEntryActivity.
	 */
	private PendingIntent createSurveyIntent() {
		Intent intent = new Intent("android.intent.action.EDIT");

		intent.setComponent(new ComponentName(ODK_COLLECT_PACKAGE, ODK_COLLECT_CMP));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setData(Uri.parse(ODK_FORMS_URI));

		return PendingIntent.getActivity(this, 0, intent, 0);
	}

	/**
	 * Create a Notification object which launches an ODK survey when clicked.
	 * 
	 * @return Notification containing a PendingIntent for an ODK survey
	 */
	private Notification createSurveyNotification() {
		NotificationCompat.Builder nb = new NotificationCompat.Builder(this)
				.setSmallIcon(android.R.drawable.ic_menu_my_calendar)
				.setContentTitle(getResources().getText(R.string.notification_title))
				.setContentText(getResources().getText(R.string.notification_message))
				.setContentIntent(createSurveyIntent());

		return nb.build();
	}

}
