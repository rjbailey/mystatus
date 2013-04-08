package com.example.launchodkfrequently;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Main extends Activity implements OnClickListener {
	private final String TAG = "ODK_LAUNCHER";
	private final int DEFAULT_PERIOD = 10 * 1000;// 10 seconds
	Button btnStart;
	Button btnCancel;
	EditText editTextFrequency;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		btnStart = (Button) findViewById(R.id.buttonStart);
		btnCancel = (Button) findViewById(R.id.buttonCancel);
		editTextFrequency = (EditText) findViewById(R.id.editTextFrequency);
		btnStart.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View view) {
		// construct intent for launching periodic service
		Intent periodicService = new Intent(Main.this, PeriodicService.class);
		// construct pending intent in order used
		PendingIntent pintent = PendingIntent.getService(Main.this, 0,
				periodicService, 0);
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		// check which button is clicked
		if (view.getId() == btnStart.getId()) {
			// get frequency
			long frequency = Integer
					.valueOf(editTextFrequency.getText().toString());
			// make sure frequency is good otherwise use default value
			frequency = (frequency > 0) ? (frequency * 1000) : DEFAULT_PERIOD;
			// set periodic service
			am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, frequency,
					frequency, pintent);
			Log.d(TAG, "Alarm is set: frequency: " + frequency / 1000
					+ " milliseconds");
		} else if (view.getId() == btnCancel.getId()) {
			// cancel alarm service
			am.cancel(pintent);
			Log.d(TAG, "Cancel button Clicked --> cancel alarm service");
		}
	}
}
