package edu.washington.cs.mystatus;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.view.View.OnKeyListener;

/**
 * SettingsActivity provides a UI for managing notification settings.
 * 
 * It is also responsible for setting up recurring notifications when
 * notifications are enabled, and cleaning up when notifications are disabled.
 * 
 * @author Jake Bailey (rjacob@cs.washington.edu)
 * @author Chuong Dao (chuongd@cs.washington.edu)
 * @author Emily Chien (eechien@cs.washington.edu)
 * @see AlarmManager
 * @see NotificationService
 */
public class SettingsActivity extends Activity {
	
	private static final String TAG = "mystatus.SettingsActivity";
	
	private static final long DEFAULT_NOTIFICATION_PERIOD = 5; // 5 seconds
	private static final long MAX_NOTIFICATION_PERIOD = 86400; // number of seconds per day

	private CheckBox mEnableNotificationsBox;
	private EditText mNotificationPeriod;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		boolean isChecked = settings.getBoolean("settings_checked", false);
		long notDefault = settings.getLong("notification_period", DEFAULT_NOTIFICATION_PERIOD);
		
		mEnableNotificationsBox = (CheckBox) findViewById(R.id.notification_checkbox);
		mNotificationPeriod = (EditText) findViewById(R.id.notification_period);

		mEnableNotificationsBox.setChecked(isChecked);
		
		mEnableNotificationsBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SharedPreferences prefs = getPreferences(MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				if (isChecked) {
					// set preference
					editor.putBoolean("settings_checked", true);
					enableNotifications();
				} else {
					// set preference
					editor.putBoolean("settings_checked", false);
					disableNotifications();
				}
				editor.commit();
			}
		});

		mNotificationPeriod.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
						(keyCode == KeyEvent.KEYCODE_ENTER)) {
					SharedPreferences prefs = getPreferences(MODE_PRIVATE);
					SharedPreferences.Editor editor = prefs.edit();
					
					editor.putLong("notification_period", Long.parseLong(mNotificationPeriod.getText().toString()));
					editor.commit();
					return true;
				}
				return false;
			}
			
		});
		
		mNotificationPeriod.setText(Long.toString(notDefault));
	}

	/**
	 * Sets a repeating AlarmManager alarm that triggers NotificationService to
	 * generate a survey notification. The period with which the notification
	 * will appear is determined by the mNotificationPeriod text field, which
	 * represents the notification period in seconds.
	 */
	private void enableNotifications() {
		Log.w(TAG, "Enabling notifications");
		PendingIntent notifyIntent = PendingIntent.getService(this, 0,
				new Intent(this, NotificationService.class), 0);
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		// Parse and validate the desired period between notifications
		long period;
		try {
			period = Long.parseLong(mNotificationPeriod.getText().toString());
		} catch (NumberFormatException e) {
			period = DEFAULT_NOTIFICATION_PERIOD;
			mNotificationPeriod.setText(Long.toString(period));
		}
		if (period <= 0 || period > MAX_NOTIFICATION_PERIOD) {
			period = DEFAULT_NOTIFICATION_PERIOD;
			mNotificationPeriod.setText(Long.toString(period));
		}
		// Convert from seconds to milliseconds
		long freqMillis = period * 1000;

		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, freqMillis, freqMillis, notifyIntent);
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
