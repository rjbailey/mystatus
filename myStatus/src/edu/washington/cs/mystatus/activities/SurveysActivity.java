package edu.washington.cs.mystatus.activities;

import edu.washington.cs.mystatus.providers.FormsProviderAPI.FormsColumns;
import edu.washington.cs.mystatus.utilities.VersionHidingCursorAdapter;

import edu.washington.cs.mystatus.R;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * SurveysActivity should provide a list of all surveys which currently need a
 * response. Surveys needing a response are those surveys that the user has
 * subscribed to, but not responded to within their set period (i.e., if a
 * survey requires a response every N days, and it has been N days since the
 * user last responded to it, it will appear in the list).
 * 
 * @author Jake Bailey (rjacob@cs.washington.edu)
 */
public class SurveysActivity extends ListActivity {

	private static final String TAG = "mystatus.SurveysActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_surveys);
		Log.d(TAG, "Surveys activity created.");

		String sortOrder = FormsColumns.DISPLAY_NAME + " ASC, " + FormsColumns.JR_VERSION + " DESC";
		Cursor c = getContentResolver()
				.query(FormsColumns.CONTENT_URI, null, null, null, sortOrder);

		String[] data = new String[] {
				FormsColumns.DISPLAY_NAME, FormsColumns.DISPLAY_SUBTEXT, FormsColumns.JR_VERSION
		};
		int[] view = new int[] {
				R.id.text1, R.id.text2, R.id.text3
		};

		// TODO: filter results based on whether the surveys need a response.
		SimpleCursorAdapter instances = new VersionHidingCursorAdapter(FormsColumns.JR_VERSION,
				this, R.layout.two_item, c, data, view);
		setListAdapter(instances);
	}

	@Override
	protected void onListItemClick(ListView listView, View view, int position, long id) {
		long idFormsTable = ((SimpleCursorAdapter) getListAdapter()).getItemId(position);
		Uri formUri = ContentUris.withAppendedId(FormsColumns.CONTENT_URI, idFormsTable);
		startActivity(new Intent(Intent.ACTION_EDIT, formUri));
	}
}
