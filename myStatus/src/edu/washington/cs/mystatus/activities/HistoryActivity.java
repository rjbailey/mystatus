package edu.washington.cs.mystatus.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.washington.cs.mystatus.R;
import edu.washington.cs.mystatus.application.MyStatus;
import edu.washington.cs.mystatus.odk.provider.InstanceProviderAPI;
import edu.washington.cs.mystatus.odk.provider.InstanceProviderAPI.InstanceColumns;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView.FindListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class HistoryActivity extends ListActivity {
	private HashMap<String, Integer> formTypes;
	private List<String> formNameList;
	private final String NOT_YET_RECORDED = "Not Yet Recorded";
	//private Button btnViewTable;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.mystatus_surveys);
		setContentView(R.layout.mystatus_surveys);
		TextView view = (TextView) findViewById(android.R.id.empty);
		view.setText(NOT_YET_RECORDED);
		//LinearLayout layout = (LinearLayout) findViewById(R.id.)
		
		// initialize hashmap for storing data types
		ArrayAdapter<String> adapter ;
		formTypes = new HashMap<String, Integer>();
		formNameList = new ArrayList<String>();
		refreshFormNameList();
		adapter = new ArrayAdapter<String>(this,
								android.R.layout.simple_list_item_1, formNameList);
		setListAdapter(adapter);
		
	}
	
	/**
	 * 
	 */
	
	private void refreshFormNameList(){
	    String sortOrder = InstanceColumns.STATUS + " DESC, "
                + InstanceColumns.DISPLAY_NAME + " ASC";
        Cursor c = managedQuery(InstanceColumns.CONTENT_URI, null, null,
                null, sortOrder);
        
        // iterate through cursor to get all form types......
        if (c.getCount() > 0){
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
        }
	}
	/**
	 * Stores the path of selected instance in the parent class and finishes.
	 */
	@Override
	protected void onListItemClick(ListView listView, View view, int position,
			long id) {
		String formName = (String) getListAdapter().getItem(position);
		// start activity to display the list of forms
		Intent intent = new Intent (this, FormTypeListActivity.class);
		intent.putExtra("formName", formName);
		startActivity(intent);
		
	}

	@Override
    protected void onResume() {
        super.onResume();
        ((MyStatus)getApplicationContext()).connectCacheWord();
       refreshFormNameList();
       // update the formname list
       setListAdapter(new ArrayAdapter<String>(this,
                                android.R.layout.simple_list_item_1, formNameList));
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
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		 // disconnect to cache word to get 
        ((MyStatus)getApplicationContext()).disconnectCacheWord();
	}

	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {		
		 if (((MyStatus)getApplicationContext()).getCacheWordHandler().isLocked() && hasFocus){
	            showLockScreen();
	        } 
	}
	
	/**
     * show lock screen if not yet initialized
     */
    void showLockScreen() {
        Intent intent = new Intent(this, LockScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("originalIntent", getIntent());
        startActivity(intent);
        finish();
    }

}
