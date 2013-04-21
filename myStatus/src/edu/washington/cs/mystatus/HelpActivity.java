package edu.washington.cs.mystatus;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * HelpActivity provides quick-dial buttons for important contacts. Eventually,
 * the user should be able to pick "support group" contacts from their Contacts
 * list and have them appear in this Activity.
 * 
 * @author Jake Bailey (rjacob@cs.washington.edu)
 */
public class HelpActivity extends Activity {

	private static final String DOCTOR_NUM = "tel:555-1234";
	private static final String EMERGENCY_NUM = "tel:911";
	private static final String LIFELINE_NUM = "tel:1-800-273-8255"; // National Suicide Prevention Lifeline

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);

		Button callDoctor = (Button) findViewById(R.id.call_doctor);
		Button callEmergencyServices = (Button) findViewById(R.id.call_emergency_services);
		Button callSuicideLifeline = (Button) findViewById(R.id.call_suicide_lifeline);
		Button callFriend = (Button) findViewById(R.id.call_friend);

		callDoctor.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialNumber(Uri.parse(DOCTOR_NUM));
			}
		});

		callEmergencyServices.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialNumber(Uri.parse(EMERGENCY_NUM));
			}
		});

		callSuicideLifeline.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialNumber(Uri.parse(LIFELINE_NUM));
			}
		});

		callFriend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});
	}

	/**
	 * Display the phone dialer with the given number filled in.
	 * 
	 * @param phoneNumber The number to dial.
	 */
	private void dialNumber(Uri phoneNumber) {
		Intent dialIntent = new Intent(Intent.ACTION_DIAL).setData(phoneNumber);
		startActivity(dialIntent);
	}
}
