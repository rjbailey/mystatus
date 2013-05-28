package edu.washington.cs.mystatus.services;

import edu.washington.cs.mystatus.R;
import edu.washington.cs.mystatus.activities.AddPrescription;
import edu.washington.cs.mystatus.activities.SurveyListTabs;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
		Bundle b = intent.getExtras();
		Notification notification = createNotification(b);
		notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(NOTIFICATION, notification);

		return super.onStartCommand(intent, flags, startId);
	}
	
	private Notification createNotification(Bundle b) {
		int hour = b.getInt("HOUR");
		int min = b.getInt("MINUTE");
		String brandName = b.getString("BRAND_NAME");
		String chemName = b.getString("CHEM_NAME");
		NotificationCompat.Builder nb = new NotificationCompat.Builder(this)
			.setSmallIcon(android.R.drawable.ic_menu_my_calendar)
			.setContentTitle(getResources().getText(R.string.pres_notification_title));
		if (min < 10)
			nb.setContentText("This is your " + hour + ":0" + min + " notification.");
		else
			nb.setContentText("This is your " + hour + ":" + min + " notification.");
		Intent i = new Intent(this, AddPrescription.class);
		i.putExtra("BRAND_NAME", brandName);
		i.putExtra("CHEM_NAME", chemName);
		PendingIntent prescriptionIntent = PendingIntent.getActivity(this, 0,
				i, Intent.FLAG_ACTIVITY_NEW_TASK);
		nb.setContentIntent(prescriptionIntent);
		return nb.build();
	}
	
}
