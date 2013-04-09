package edu.washington.cs.mystatus;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class SurveysActivity extends Activity {

	private static final String TAG = "myStatus";

	private static final String ODK_COLLECT_PACKAGE = "org.odk.collect.android";
	private static final String ODK_COLLECT_CMP = "org.odk.collect.android.activities.FormEntryActivity";
	private static final String ODK_FORMS_URI = "content://org.odk.collect.android.provider.odk.forms/forms/1";

	private Button mStartBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_surveys);

		mStartBtn = (Button) findViewById(R.id.surveys_start);

		mStartBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Survey button clicked");
				startSurvey();
			}
		});
	}

	/**
	 * Start the ODK Collect FormEntryActivity for the first downloaded survey.
	 */
	private void startSurvey() {
		Log.w(TAG, "Starting ODK Collect survey...");
		Intent intent = new Intent("android.intent.action.EDIT");
		intent.setComponent(new ComponentName(ODK_COLLECT_PACKAGE, ODK_COLLECT_CMP));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setData(Uri.parse(ODK_FORMS_URI));
		startActivityForResult(intent, 0);
	}

}
