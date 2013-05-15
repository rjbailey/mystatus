package edu.washington.cs.mystatus.activities;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import edu.washington.cs.mystatus.R;

/**
 * Displays tabs for the list of passive surveys. Shows one tab for the surveys
 * that are currently due, and another for a list of all surveys.
 * 
 * @author Jake Bailey (rjacob@cs.washington.edu)
 */
@SuppressWarnings("deprecation")
public class SurveyListTabs extends TabActivity {

	private static final String DUE_TAB = "due_tab";
	private static final String ALL_TAB = "all_tab";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(getString(R.string.app_name));

		final TabHost tabHost = getTabHost();

		Intent dueList = new Intent(this, DueSurveysList.class);
		tabHost.addTab(tabHost.newTabSpec(DUE_TAB).setIndicator(getString(R.string.due_surveys))
				.setContent(dueList));

		Intent allList = new Intent(this, AllSurveysList.class);
		tabHost.addTab(tabHost.newTabSpec(ALL_TAB).setIndicator(getString(R.string.all_surveys))
				.setContent(allList));
	}
}
