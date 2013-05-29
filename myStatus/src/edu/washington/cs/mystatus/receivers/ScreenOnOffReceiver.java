package edu.washington.cs.mystatus.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//broadcast receiver used for detecting screen timeout
public class ScreenOnOffReceiver extends BroadcastReceiver {
	public boolean wasOffBefore = false;
	public boolean wasOnBefore = true;
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			wasOnBefore = true;
			wasOffBefore = false;
		} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
			wasOffBefore = true;
			wasOnBefore = false;
		}
	}
	

}