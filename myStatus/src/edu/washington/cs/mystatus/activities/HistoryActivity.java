package edu.washington.cs.mystatus.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.washington.cs.mystatus.R;
import edu.washington.cs.mystatus.application.MyStatus;
import edu.washington.cs.mystatus.providers.InstanceProviderAPI;
import edu.washington.cs.mystatus.providers.InstanceProviderAPI.InstanceColumns;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HistoryActivity extends ListActivity {
	private HashMap<String, Integer> formTypes;
	private List<String> formNameList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chooser_list_layout);
		// initialize hashmap for storing data types
		// @CD
		formTypes = new HashMap<String, Integer>();
		formNameList = new ArrayList<String>();
		String selection = InstanceColumns.STATUS + " != ?";
		String[] selectionArgs = { InstanceProviderAPI.STATUS_SUBMITTED };
		String sortOrder = InstanceColumns.STATUS + " DESC, "
				+ InstanceColumns.DISPLAY_NAME + " ASC";
		Cursor c = managedQuery(InstanceColumns.CONTENT_URI, null, selection,
				selectionArgs, sortOrder);

		// iterate through cursor to get all form types......
		// @CD
		c.moveToFirst();
		do {
			String key = c.getString(c
					.getColumnIndex(InstanceColumns.DISPLAY_NAME));
			if (!formTypes.containsKey(key)) {
				// add new name to the list
				formTypes.put(key, 0);
				formNameList.add(key);
			} else {
				// keep counting for reference
				int oldCount = formTypes.get(key);
				formTypes.put(key, ++oldCount);
			}

		} while (c.moveToNext());
		// return cursor to the beginning
		c.moveToFirst();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, formNameList);

		setListAdapter(adapter);
	}

	/**
	 * Stores the path of selected instance in the parent class and finishes.
	 */
	@Override
	protected void onListItemClick(ListView listView, View view, int position,
			long id) {
		String formName = (String) getListAdapter().getItem(position);
		// start activity to display the list of forms
		// @CD
		Intent intent = new Intent (this, FormTypeListActivity.class);
		intent.putExtra("formName", formName);
		startActivity(intent);
		
	}

	@Override
	protected void onStart() {
		super.onStart();
		MyStatus.getInstance().getActivityLogger().logOnStart(this);
	}

	@Override
	protected void onStop() {
		MyStatus.getInstance().getActivityLogger().logOnStop(this);
		super.onStop();
	}
	


}
