package edu.washington.cs.mystatus;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;

/**
 * OdkProxy is responsible for centralizing and simplifying communication
 * between the core myStatus features and ODK Collect features.
 * 
 * @author Jake Bailey (rjacob@cs.washington.edu)
 * @author Chuong Dao (chuongd@cs.washington.edu)
 */
public class OdkProxy {

	private static final String ODK_COLLECT_PACKAGE = "org.odk.collect.android";
	private static final String ODK_COLLECT_CMP = "org.odk.collect.android.activities.FormEntryActivity";
	private static final String ODK_FORMS_URI = "content://org.odk.collect.android.provider.odk.forms/forms/1";

	/**
	 * Return an Intent for ODK Collect's FormEntryActivity, using the first
	 * survey in ODK's survey list.
	 * 
	 * @return An Intent which launches an ODK Collect FormEntryActivity.
	 */
	public static Intent createSurveyIntent() {
		return new Intent("android.intent.action.EDIT")
				.setComponent(new ComponentName(ODK_COLLECT_PACKAGE, ODK_COLLECT_CMP))
				.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
				.setData(Uri.parse(ODK_FORMS_URI));
	}
}
