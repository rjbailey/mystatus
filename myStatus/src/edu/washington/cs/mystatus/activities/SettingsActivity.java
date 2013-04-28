package edu.washington.cs.mystatus.activities;

import edu.washington.cs.mystatus.R;
import edu.washington.cs.mystatus.services.NotificationService;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.CalendarContract;
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
	}

}
