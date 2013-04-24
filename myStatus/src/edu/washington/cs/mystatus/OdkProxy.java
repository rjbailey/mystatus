package edu.washington.cs.mystatus;

import org.odk.collect.android.activities.FormChooserList;
import org.odk.collect.android.activities.FormDownloadList;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.activities.InstanceChooserList;

import android.content.Context;
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

	private static final String ODK_FORMS_URI = "content://org.odk.collect.android.provider.odk.forms/forms/1";

	/**
	 * Return an Intent for ODK Collect's FormEntryActivity, using the first
	 * survey in ODK's survey list.
	 * 
	 * @return An Intent which launches an ODK Collect FormEntryActivity.
	 */
	public static Intent createSurveyIntent(Context context) {
//		return new Intent(context, FormEntryActivity.class)
//				.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//				.setData(Uri.parse(ODK_FORMS_URI));
		return new Intent(context, AlertDialogActivity.class)
		.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		.setData(Uri.parse(ODK_FORMS_URI));
	}

	/**
	 * Create an Intent for Collect's FormChooserList.
	 * 
	 * @return An Intent which launches the ODK Collect FormChooserList.
	 */
	public static Intent createFormChooserIntent(Context context) {
		return new Intent(context, FormChooserList.class);
	}

	/**
	 * Create an Intent for Collect's FormDownloadList.
	 * 
	 * @return An Intent which launches the ODK Collect FormDownloadList.
	 */
	public static Intent createFormDownloadIntent(Context context) {
		//return new Intent(context, FormDownloadList.class);
		return new Intent(context, InstanceChooserList.class);
	}
}
