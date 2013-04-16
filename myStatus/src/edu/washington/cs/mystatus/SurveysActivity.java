package edu.washington.cs.mystatus;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * SurveysActivity should provide a list of all surveys which currently need a
 * response. Surveys needing a response are those surveys that the user has
 * subscribed to, but not responded to within their set period (i.e., if a
 * survey requires a response every N days, and it has been N days since the
 * user last responded to it, it will appear in the list).
 * 
 * @author Jake Bailey (rjacob@cs.washington.edu)
 */
public class SurveysActivity extends Activity {

	private static final String TAG = "mystatus.SurveysActivity";

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
	 * Start the ODK Collect FormEntryActivity for the first survey in ODK
	 * Collect's survey list.
	 */
	private void startSurvey() {
		Log.w(TAG, "Starting ODK Collect survey...");
		startActivityForResult(OdkProxy.createSurveyIntent(this), 0);
	}
}
