package edu.washington.cs.mystatus.activities;

import edu.washington.cs.mystatus.R;
import edu.washington.cs.mystatus.application.MyStatus;
import edu.washington.cs.mystatus.providers.FormsProviderAPI.FormsColumns;
import edu.washington.cs.mystatus.providers.InstanceProvider;
import edu.washington.cs.mystatus.providers.InstanceProviderAPI;
import edu.washington.cs.mystatus.providers.InstanceProviderAPI.InstanceColumns;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class FormTypeListActivity extends ListActivity {
	private static final boolean EXIT = true;
	private static final boolean DO_NOT_EXIT = false;
	private AlertDialog mAlertDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// get the form wanted to be displayed
		Intent intent = this.getIntent();
		// String content the key of the form
		String formName = intent.getExtras().getString("formName");
		setContentView(R.layout.chooser_list_layout);
		setTitle(getString(R.string.app_name) + " > "
				+ getString(R.string.review_data));
		TextView tv = (TextView) findViewById(R.id.status_text);
		tv.setVisibility(View.GONE);
		// edit selection to get only the form needed
		String selection = InstanceColumns.STATUS + " != ? "+ "OR "
						  +InstanceColumns.STATUS+ " != ? " +"AND "
				+ InstanceColumns.DISPLAY_NAME + " = " + "\"" +formName+"\"";
		//String[] selectionArgs = { InstanceProviderAPI.STATUS_SUBMITTED };
		String[] selectionArgs = { InstanceProviderAPI.STATUS_COMPLETE, InstanceProviderAPI.STATUS_SUBMITTED};
		String sortOrder = InstanceColumns.STATUS + " DESC, "
				+ InstanceColumns.DISPLAY_NAME + " ASC";
		Cursor c = managedQuery(InstanceColumns.CONTENT_URI, null, selection,
				selectionArgs, sortOrder);

		String[] data = new String[] { InstanceColumns.DISPLAY_NAME,
				InstanceColumns.DISPLAY_SUBTEXT };
		int[] view = new int[] { R.id.text1, R.id.text2 };

		// render total instance view
//		SimpleCursorAdapter instances = new SimpleCursorAdapter(this,
//				R.layout.two_item, c, data, view);
		// render total instance view
		MySimpleCursorAdapter instances = new MySimpleCursorAdapter(this,
					c);
		setListAdapter(instances);
	}

	/**
	 * Stores the path of selected instance in the parent class and finishes.
	 */
	@Override
	protected void onListItemClick(ListView listView, View view, int position,
			long id) {
		Cursor c = (Cursor) getListAdapter().getItem(position);
		startManagingCursor(c);
		Uri instanceUri = ContentUris.withAppendedId(
				InstanceColumns.CONTENT_URI,
				c.getLong(c.getColumnIndex(InstanceColumns._ID)));
		
		MyStatus.getInstance().getActivityLogger()
				.logAction(this, "onListItemClick", instanceUri.toString());

		String action = getIntent().getAction();
		if (Intent.ACTION_PICK.equals(action)) {
			// caller is waiting on a picked form
			setResult(RESULT_OK, new Intent().setData(instanceUri));
		} else {
			// the form can be edited if it is incomplete or if, when it was
			// marked as complete, it was determined that it could be edited
			// later.
			String status = c.getString(c
					.getColumnIndex(InstanceColumns.STATUS));
			String strCanEditWhenComplete = c.getString(c
					.getColumnIndex(InstanceColumns.CAN_EDIT_WHEN_COMPLETE));

			boolean canEdit = status
					.equals(InstanceProviderAPI.STATUS_INCOMPLETE)
					|| Boolean.parseBoolean(strCanEditWhenComplete);
			if (!canEdit) {
				createErrorDialog(
						getString(R.string.cannot_edit_completed_form),
						DO_NOT_EXIT);
				return;
			}
			// caller wants to view/edit a form, so launch formentryactivity
			Intent intent = new Intent (this, FormEntryActivity.class);
			intent.setData(instanceUri);
			intent.putExtra("formUri", getFormsUri(c.getString
													(c.getColumnIndex
															(InstanceColumns.DISPLAY_NAME))));
			startActivity(intent);
			//startActivity(new Intent(Intent.ACTION_EDIT, instanceUri, this, FormEntryActivity.class));
		}
		finish();
	}
	
	private String getFormsUri (String instanceDisplayName){
		// edit selection to get only the form needed
		String selection = FormsColumns.DISPLAY_NAME + " = " + "\"" +instanceDisplayName+"\"";
//		 String[] projection = {
//                 FormsColumns._ID, FormsColumns.FORM_FILE_PATH
//         };
		 
		Cursor c = managedQuery(FormsColumns.CONTENT_URI, null, selection,
				null, null);
		startManagingCursor(c);
		c.moveToFirst();
		Uri formUri = ContentUris.withAppendedId(
				FormsColumns.CONTENT_URI,
				c.getLong(c.getColumnIndex(FormsColumns._ID)));
		return formUri.toString();
		
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

	private void createErrorDialog(String errorMsg, final boolean shouldExit) {
		MyStatus.getInstance().getActivityLogger()
				.logAction(this, "createErrorDialog", "show");

		mAlertDialog = new AlertDialog.Builder(this).create();
		mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
		mAlertDialog.setMessage(errorMsg);
		DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int i) {
				switch (i) {
				case DialogInterface.BUTTON1:
					MyStatus.getInstance()
							.getActivityLogger()
							.logAction(this, "createErrorDialog",
									shouldExit ? "exitApplication" : "OK");
					if (shouldExit) {
						finish();
					}
					break;
				}
			}
		};
		mAlertDialog.setCancelable(false);
		mAlertDialog.setButton(getString(R.string.ok), errorListener);
		mAlertDialog.show();
	}
	// Helper used to show better list in history
	public class MySimpleCursorAdapter extends CursorAdapter {

		public MySimpleCursorAdapter(Context context, Cursor c) {
			super(context, c);
		}


	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView textViewLabel = (TextView) view.findViewById(R.id.label);
		TextView textViewSave = (TextView) view.findViewById(R.id.status);
	    ImageView imageView = (ImageView) view.findViewById(R.id.icon);
	    String labelString = cursor.getString(cursor.getColumnIndex(InstanceColumns.DISPLAY_NAME));
	    String savedString = cursor.getString(cursor.getColumnIndex(InstanceColumns.DISPLAY_SUBTEXT));
	    textViewLabel.setText(labelString);
	    textViewSave.setText(savedString);
	    if (savedString.contains("Saved")){
	    	// TODO: set icon for saved item
	    	imageView.setImageResource(R.drawable.btn_star_big_off);
	    }else if (savedString.contains("Finalized")){
	    	// TODO: set icon for submitted item
	    	imageView.setImageResource(R.drawable.btn_star_big_on_disable);
	    }else {
	    	// TODO: set icon for submitted item
	    	imageView.setImageResource(R.drawable.btn_star_big_on_selected);
	    }
	}

	@Override
	public View newView(Context ctx, Cursor context, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) ctx
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = inflater.inflate(R.layout.row_layout, parent, false);
		return view;
	}
	} 
}
