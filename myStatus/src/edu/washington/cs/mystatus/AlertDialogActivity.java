package edu.washington.cs.mystatus;

import org.odk.collect.android.activities.FormChooserList;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
public class AlertDialogActivity extends Activity {
	private AlertDialog.Builder alertDialog;
	private static final String MESSAGE = "Would you like to complete the survey now ?";
	private static final String DISMISS_BTN_NAME = "Dismiss";
	private static final String START_BTN_NAME = "Start";
	private static final String TAG = "ALERT_DIALOG";
	private static final int FOUR_HOURS = 4 * 60;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		alertDialog = new AlertDialog.Builder(this);
		alertDialog.setMessage(MESSAGE)
	               .setNegativeButton(DISMISS_BTN_NAME, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// disable old notifications
						disableNotifications();
						// push the notification longer
						enableNotificationsWithArgs(FOUR_HOURS);
					}
				}).setPositiveButton(START_BTN_NAME, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// bring user to the list for now
						startActivity(new Intent(AlertDialogActivity.this, FormChooserList.class));
					}
					
				});
		alertDialog.show();
		// close the activity
		//this.finish();
	          
	}
	
	/**
	 * Adding this class allow to reschedule the notification
	 * @param period
	 */
	private void enableNotificationsWithArgs(int period) {
		// Convert from seconds to milliseconds
		PendingIntent notifyIntent = PendingIntent.getService(this, 0,
				new Intent(this, NotificationService.class), 0);
		long freqMillis = period * 1000;
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+freqMillis,
				freqMillis, notifyIntent);
		Log.d(TAG, "Notifying every " + period + " seconds");
	}
	
	/**
	 * Cancels the AlarmManager alarm that generates notifications.
	 */
	private void disableNotifications() {
		Log.w(TAG, "Disabling notifications");
		PendingIntent notificationIntent = PendingIntent.getService(this, 0,
				new Intent(this, NotificationService.class), 0);
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		am.cancel(notificationIntent);
	}
}
