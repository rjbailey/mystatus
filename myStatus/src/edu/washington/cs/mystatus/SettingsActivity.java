package edu.washington.cs.mystatus;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

/**
 * SettingsActivity provides a UI for managing notification settings.
 * 
 * It is also responsible for setting up recurring notifications when
 * notifications are enabled, and cleaning up when notifications are disabled.
 * 
 * @author Jake Bailey (rjacob@cs.washington.edu)
 * @author Chuong Dao (chuongd@cs.washington.edu)
 * @see AlarmManager
 * @see NotificationService
 */
public class SettingsActivity extends Activity {

	private static final String TAG = "mystatus.SettingsActivity";

	private static final long DEFAULT_NOTIFICATION_PERIOD = 5; // 5 seconds
	private static final long MAX_NOTIFICATION_PERIOD = 86400; // number of seconds per day

	private CheckBox mEnableNotificationsBox;
	private EditText mNotificationFrequency;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		mEnableNotificationsBox = (CheckBox) findViewById(R.id.notification_checkbox);
		mNotificationFrequency = (EditText) findViewById(R.id.notification_frequency);

		mEnableNotificationsBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					enableNotifications();
				} else {
					disableNotifications();
				}
			}
		});

		mNotificationFrequency.setText(Long.toString(DEFAULT_NOTIFICATION_PERIOD));
	}

	/**
	 * Sets a repeating AlarmManager alarm that triggers NotificationService to
	 * generate a survey notification. The frequency with which the notification
	 * will appear is determined by the mNotificationFrequency text field, which
	 * represents the notification period in seconds.
	 */
	private void enableNotifications() {
		Log.w(TAG, "Enabling notifications");
		PendingIntent notifyIntent = PendingIntent.getService(this, 0,
				new Intent(this, NotificationService.class), 0);
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		long frequency;
		try {
			frequency = Long.parseLong(mNotificationFrequency.getText().toString());
		} catch (NumberFormatException e) {
			frequency = DEFAULT_NOTIFICATION_PERIOD;
		}
		if (frequency <= 0 || frequency > MAX_NOTIFICATION_PERIOD) {
			frequency = DEFAULT_NOTIFICATION_PERIOD;
			mNotificationFrequency.setText(Long.toString(frequency));
		}
		// Convert from seconds to milliseconds
		long freqMillis = frequency * 1000;

		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, freqMillis, freqMillis, notifyIntent);
		Log.d(TAG, "Notifying every " + frequency + " seconds");
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
