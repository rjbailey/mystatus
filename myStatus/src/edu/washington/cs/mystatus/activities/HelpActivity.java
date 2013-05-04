package edu.washington.cs.mystatus.activities;

import edu.washington.cs.mystatus.R;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * HelpActivity provides quick-dial buttons for important contacts. Eventually,
 * the user should be able to pick "support group" contacts from their Contacts
 * list and have them appear in this Activity.
 * 
 * @author Jake Bailey (rjacob@cs.washington.edu)
 */
public class HelpActivity extends Activity {

	private static final String TAG = "mystatus.HelpActivity";

	private static final int PICK_CONTACT_REQUEST = 0;

	private static final String DOCTOR_NUM = "tel:555-1234";
	private static final String EMERGENCY_NUM = "tel:911";
	private static final String LIFELINE_NUM = "tel:1-800-273-8255"; // National Suicide Prevention Lifeline

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mystatus_help);
		Log.d(TAG, "Help activity created.");

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
				Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
				pickContactIntent.setType(Phone.CONTENT_TYPE); // only contacts w/ phone numbers
				startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case PICK_CONTACT_REQUEST:
			if (resultCode == RESULT_OK) {
				Uri contactUri = data.getData();
				String[] projection = { Phone.NUMBER };

				Cursor cursor = getContentResolver()
						.query(contactUri, projection, null, null, null);
				cursor.moveToFirst();

				// Retrieve the phone number from the NUMBER column
				int column = cursor.getColumnIndex(Phone.NUMBER);
				String number = cursor.getString(column);
				cursor.close();

				dialNumber(Uri.parse("tel:" + number));
			} else {
				Toast.makeText(this, "Couldn't get phone number for contact.", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}
}
