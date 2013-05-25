package edu.washington.cs.mystatus.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;

import org.spongycastle.crypto.DataLengthException;
import org.spongycastle.crypto.InvalidCipherTextException;

import edu.washington.cs.mystatus.R;
import edu.washington.cs.mystatus.application.MyStatus;
import edu.washington.cs.mystatus.providers.FormsProviderAPI.FormsColumns;
import edu.washington.cs.mystatus.providers.InstanceProvider;
import edu.washington.cs.mystatus.providers.InstanceProviderAPI;
import edu.washington.cs.mystatus.providers.InstanceProviderAPI.InstanceColumns;
import edu.washington.cs.mystatus.utilities.DataEncryptionUtils;
import edu.washington.cs.mystatus.utilities.FileUtils;
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
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class FormTypeListActivity extends ListActivity {
	private static final boolean EXIT = true;
	private static final boolean DO_NOT_EXIT = false;
	private AlertDialog mAlertDialog;
	private Button btnViewTable;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// get the form wanted to be displayed
		Intent intent = this.getIntent();
		// String content the key of the form
		String formName = intent.getExtras().getString("formName");
		setContentView(R.layout.mysurveys_with_view_btn);
		setTitle(getString(R.string.app_name) + " > "
				+ getString(R.string.review_data));
		// edit selection to get only the form needed
		String selection = InstanceColumns.DISPLAY_NAME + " = " + "\"" +formName+"\"";
		//String[] selectionArgs = { InstanceProviderAPI.STATUS_SUBMITTED };
		//String[] selectionArgs = { InstanceProviderAPI.STATUS_COMPLETE, InstanceProviderAPI.STATUS_SUBMITTED};
		String sortOrder = InstanceColumns.STATUS + " DESC, "
				+ InstanceColumns.DISPLAY_NAME + " ASC";
		final Cursor c = managedQuery(InstanceColumns.CONTENT_URI, null, selection,
				null, sortOrder);
		
		// add button for display table
		// @CD
		btnViewTable = (Button) findViewById(R.id.viewAsTable);
		btnViewTable.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
              // need to give the display history table path to the instance
              if (c.moveToFirst()){
            	  String instancePath = c.getString(c.getColumnIndex(InstanceColumns.INSTANCE_FILE_PATH));
            	  instancePath = instancePath.substring(0,instancePath.lastIndexOf("/"));	
            	  Intent displayIntent = new Intent(FormTypeListActivity.this, DisplayHistoryAsTable.class);
            	  displayIntent.putExtra("instancePath", instancePath);
                  startActivity(displayIntent);
              }else {
            	  // TODO: display some TOAST about not having any history
              }
             
            }
        });
		
		
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
			// decrypt data here....
			Cursor instanceCursor = getContentResolver().query(instanceUri,
					null, null, null, null);
			if (instanceCursor.moveToFirst()){
				String instanceRealPath = instanceCursor
						.getString(instanceCursor
								.getColumnIndex(InstanceColumns.INSTANCE_FILE_PATH));
				// check temp instance folder
				File instanceTempDir = new File (MyStatus.TEMP_INSTANCE_PATH);
				
				// create the temporary instance folder if it's not yet exist
				if (!instanceTempDir.exists()){
					FileUtils.createFolder(MyStatus.TEMP_INSTANCE_PATH);
				}
				
				// create instance folder inside temp if it's not yet exist
				String instanceFolderName = instanceRealPath.substring
						(instanceRealPath.lastIndexOf("/"),
						 instanceRealPath.indexOf(".xml"));
				String instanceXMLPath = MyStatus.TEMP_INSTANCE_PATH + File.separator + 
											instanceFolderName+File.separator+instanceFolderName + ".xml";
				File tempInsFolder = new File(instanceFolderName);
				if (!tempInsFolder.exists()){
					FileUtils.createFolder(MyStatus.TEMP_INSTANCE_PATH+File.separator+instanceFolderName);
				}
					
				// decrypt data here ---need to catch some execption for security reason
				DataEncryptionUtils ec = new DataEncryptionUtils();
				ec.InitCiphers();
				FileInputStream in;
				try {
					in = new FileInputStream(instanceRealPath);
					FileOutputStream out = new FileOutputStream(instanceXMLPath);
					ec.CBCDecrypt(in, out);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (DataLengthException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ShortBufferException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalBlockSizeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BadPaddingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidCipherTextException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				
				
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
