package com.example.launchodkfrequently;

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

public class PeriodicService extends Service {
	private final String TAG = "ODK_LAUNCHER";
	private static final int NOTIFICATION_ID = 32767;
	private static final String MSG = "It's time to fill out your survey";
	private static final String TITLE = "New Survey";
	private static final String INTENT_ACTION_EDIT = "android.intent.action.EDIT";
	private static final String ODK_COLLECT_PACKAGE = "org.odk.collect.android";
	private static final String ODK_COLLECT_CMP = "org.odk.collect.android.activities.FormChooserList";
	private static final String ODK_COLLECT_DATA = "content://org.odk.collect.android.provider.odk.forms/forms/1";

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// build a notification
		Notification collectNotification = constructCollectNotification();
		// push a notification to leaD to launching ODK
		NotificationManager nm = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		// notify user 
		nm.notify(NOTIFICATION_ID, collectNotification);
		Log.d(TAG, "Periodic Service start notification");
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 *  construct the intent launcher for ODK collect
	 * @return PendingIntent used for launch ODK
	 */
	private PendingIntent getODKCollectIntent() {
		Intent intent = new Intent("android.intent.action.MAIN");
		intent.setComponent(new ComponentName(ODK_COLLECT_PACKAGE,
				ODK_COLLECT_CMP));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setData(Uri.parse(ODK_COLLECT_DATA));
		// make pending intent used for launching activtiy in notification
		PendingIntent pintent = PendingIntent.getActivity(this, 0, intent,
				0);
		Log.d(TAG, "ODK collect intent built");
		return pintent;
	}

	/**
	 * this method used for constructing an notification which can lead to
	 * launching the odk intent
	 * 
	 * @return Notification
	 */
	private Notification constructCollectNotification() {
		int icon = android.R.drawable.ic_menu_my_calendar;
		// construct pending intent to sta
		PendingIntent contentIntent = getODKCollectIntent();
		// add features to notification
		NotificationCompat.Builder nb = new NotificationCompat.Builder(this)
				.setSmallIcon(icon).setContentTitle(TITLE).setContentText(MSG);
		nb.setContentIntent(contentIntent);
		Log.d(TAG, "Construct Collect notification");
		return nb.build();
	}

}
