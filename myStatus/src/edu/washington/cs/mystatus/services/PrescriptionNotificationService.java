package edu.washington.cs.mystatus.services;

import edu.washington.cs.mystatus.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * 
 * @author Emily Chien (eechien@cs.washington.edu)
 *
 */

public class PrescriptionNotificationService extends Service {
	private static final String TAG = "mystatus.PrescriptionNotificationService";
	private final int NOTIFICATION = 0;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.w(TAG, "Generating notification");
		Notification notification = createNotification();
		notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(NOTIFICATION, notification);

		return super.onStartCommand(intent, flags, startId);
	}
	
	private Notification createNotification() {
		NotificationCompat.Builder nb = new NotificationCompat.Builder(this)
			.setSmallIcon(android.R.drawable.ic_menu_my_calendar)
			.setContentTitle(getResources().getText(R.string.pres_notification_title))
			.setContentText(getResources().getText(R.string.pres_notification_title));
		// TODO: create an intent that sends user to myStatus and a picture of the prescription
		return nb.build();
	}
	
}
