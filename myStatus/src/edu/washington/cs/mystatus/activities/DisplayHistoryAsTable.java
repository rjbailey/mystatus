package edu.washington.cs.mystatus.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;

import org.spongycastle.crypto.DataLengthException;
import org.spongycastle.crypto.InvalidCipherTextException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.CalendarContract.Instances;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import edu.washington.cs.mystatus.R;
import edu.washington.cs.mystatus.application.MyStatus;
import edu.washington.cs.mystatus.listeners.FormLoaderListener;
import edu.washington.cs.mystatus.logic.FormController;
import edu.washington.cs.mystatus.logic.HierarchyElement;
import edu.washington.cs.mystatus.providers.FormsProviderAPI.FormsColumns;
import edu.washington.cs.mystatus.providers.InstanceProviderAPI.InstanceColumns;
import edu.washington.cs.mystatus.tasks.FormLoaderTask;
import edu.washington.cs.mystatus.utilities.DataEncryptionUtils;
import edu.washington.cs.mystatus.utilities.FileUtils;

public class DisplayHistoryAsTable extends Activity implements FormLoaderListener{
	TableLayout tbl;
	ArrayList<String> instanceList;
	ArrayList<String> reservedList;
	ArrayList<ArrayList<HierarchyElement>> instanceData;
	ProgressDialog progressDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table_layout_list);
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		instanceData = new ArrayList<ArrayList<HierarchyElement>>();
		instanceList = new ArrayList<String>();
		reservedList = new ArrayList<String>();
		tbl = (TableLayout)findViewById(R.id.dataTableLayout);
		//FileUtils.parseXML(xmlFile);
		String myinstancePath = getIntent().getStringExtra("instancePath");
		String instancePathPrefix = myinstancePath.substring(myinstancePath.lastIndexOf("/")+1,
														myinstancePath.indexOf("_"));
		String rootInstancePath = MyStatus.INSTANCES_PATH;
		File f = new File (rootInstancePath);
		// get all the instances
		if (f.isDirectory()){
			String [] folderList = f.list();
			for (String fn : folderList){
				String comparePrefix = fn.substring(0, fn.indexOf("_"));
				if (comparePrefix.equals(instancePathPrefix)){
					File tempFile = new File(f.getAbsolutePath()+"/"+fn);
					String [] tempFolder = tempFile.list();
					for (String instance: tempFolder){
						instanceList.add(tempFile.getAbsolutePath()+"/"+instance);
					}
				}
							
			}
			if (instanceList.size() > 0)
				this.loadingComplete(null);
			// start getting
		}
		
	}
	
	 @Override
	    public void loadingComplete(FormLoaderTask task) {
		 	// first call to complete
		 	if (task == null && instanceList.size() > 0){
		 		progressDialog.show();
		 		String instancePath = instanceList.remove(0);
		 		reservedList.add(instancePath);
		 		// change the path and decryption here
		 		// @CD
		 		String instanceRealPath = instancePath;
				// decrypt form needs to be submit here
				 decryptFormNeedtoBeUploaded(instanceRealPath);
				// construct the foler path
				// @CD
				String instanceFolderName = instanceRealPath.substring
											(instanceRealPath.lastIndexOf("/"),
											 instanceRealPath.indexOf(".xml"));
				// the newly constructed instance path
				// @CD
				String instance = MyStatus.TEMP_INSTANCE_PATH + File.separator 
										+ instanceFolderName + File.separator
										+ instanceFolderName+".xml";
		 		FormLoaderTask loaderTask = new FormLoaderTask(instance, null, null);
		 		loaderTask.setFormLoaderListener(DisplayHistoryAsTable.this);
				String formPath = getFormPath(instancePath);
				loaderTask.execute(formPath);	
		 	}else if (task != null && instanceList.size() > 0){
		 		// get the next task and put the info into list
		 		FormController formController = task.getFormController();
		        MyStatus.getInstance().setFormController(formController);
//		        // add data to instance data
		        instanceData.add(FileUtils.getHierarchyElements()); 
		        String instancePath = instanceList.remove(0);
		        reservedList.add(instancePath);
		     // change the path and decryption here
		 		// @CD
		 		String instanceRealPath = instancePath;
				// decrypt form needs to be submit here
				 decryptFormNeedtoBeUploaded(instanceRealPath);
				// construct the foler path
				// @CD
				String instanceFolderName = instanceRealPath.substring
											(instanceRealPath.lastIndexOf("/"),
											 instanceRealPath.indexOf(".xml"));
				// the newly constructed instance path
				// @CD
				String instance = MyStatus.TEMP_INSTANCE_PATH + File.separator 
										+ instanceFolderName + File.separator
										+ instanceFolderName+".xml";
		 		FormLoaderTask loaderTask = new FormLoaderTask(instance, null, null);
		 		//FormLoaderTask loaderTask = new FormLoaderTask(instancePath, null, null);
		 		loaderTask.setFormLoaderListener(DisplayHistoryAsTable.this);
				String formPath = getFormPath(instancePath);
				loaderTask.execute(formPath);
		 	} else if (task != null && instanceList.size() == 0){
		 		// loading complete ... now display data as tabular form
//		 	   // get the title
		 		// lengthy process need to be put on the thread
		 		FormController formController = task.getFormController();
		        MyStatus.getInstance().setFormController(formController);

		        //data.add(0, status);
		        // add data to instance data
		        instanceData.add(FileUtils.getHierarchyElements()); 
		 		new Thread(){
		 			public void run(){
		 				ArrayList<String> title = new ArrayList<String>();
				        ArrayList<ArrayList<String>> values = new ArrayList<ArrayList<String>>();
				        ArrayList<HierarchyElement> elements = instanceData.get(0);
				        // adding status columns
				        for (int m = 0; m < reservedList.size(); m++){
					        String oldinstancePath = reservedList.get(m);
					        String selection = InstanceColumns.INSTANCE_FILE_PATH + " = "+"\""+oldinstancePath+"\"";
					        Cursor c = managedQuery(InstanceColumns.CONTENT_URI, null, selection,
					        				null, null);
					        c.moveToFirst();
					        // adding the status to data
					        HierarchyElement status = new HierarchyElement("Status", c.getString(c.getColumnIndex
					        								(InstanceColumns.DISPLAY_SUBTEXT)), null, 0, 0, null);
					        // adding status
					        ArrayList<HierarchyElement> myelement = instanceData.get(m);
					        myelement.add(0, status);
				        }
				        
				        // get the title for the columns
				        for(int i = 0; i < elements.size(); i++){
				        	HierarchyElement el = elements.get(i);
				        	if (el.getSecondaryText() != null){
				        		title.add(el.getPrimaryText());
				        	}
				        }
				        // now extract all the information
				        for (int r = 0; r < instanceData.size(); r++){
				        	ArrayList<HierarchyElement> el = instanceData.get(r);
				        	// add new row
				        	values.add(r,new ArrayList<String>());
				        	for (int c = 0; c < el.size(); c++){
				        		HierarchyElement he = el.get(c);
				        		if (he.getSecondaryText() != null){
				        			values.get(r).add(he.getSecondaryText());
				        		}
				        	}
				        }
				        // finally inflate the table views
				        // for rows
				        LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,
				                LayoutParams.WRAP_CONTENT);
				        TableRow titleRow = new TableRow (DisplayHistoryAsTable.this);
				        //titleRow.setScrollbarFadingEnabled(false);
				        TableRow.LayoutParams rowStyle = new TableRow.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT);
				        for (int j = 0; j < title.size(); j ++){
				            TextView tv = new TextView (DisplayHistoryAsTable.this);
				            tv.setText(title.get(j));
				            tv.setPadding(10, 10, 10, 10);
				            tv.setBackgroundResource(R.drawable.cell_shape);
				            tv.setTextColor(Color.WHITE);
				            tv.setGravity(Gravity.RIGHT);
				            //tv.setScrollbarFadingEnabled(false);
				            titleRow.addView(tv, params);     
				                        
				        }
				        tbl.addView(titleRow, rowStyle);
				        // display data
				        
				        // add data test
				        for (int k = 0; k < values.size(); k++){
				        	TableRow valuesRow = new TableRow (DisplayHistoryAsTable.this);
				        	ArrayList<String> newEl = values.get(k);
				        	for (int j = 0; j < newEl.size(); j ++){
					            TextView tv = new TextView (DisplayHistoryAsTable.this);
					            tv.setText(newEl.get(j));
					            tv.setPadding(10, 10, 10, 10);
					            tv.setBackgroundResource(R.drawable.cell_shape);
					            tv.setTextColor(Color.WHITE);
					            tv.setGravity(Gravity.RIGHT);
					            //tv.setScrollbarFadingEnabled(false);
					            valuesRow.addView(tv, params);     
					            
					        }
				        	tbl.addView(valuesRow, rowStyle);
				        }
				        // turn off dialog.
				        progressDialog.cancel();
		 			}
		 		}.run();	
		 	}  
	    }

	    private void decryptFormNeedtoBeUploaded(String instanceRealPath) {
	    	// TODO Auto-generated method stub
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

		@Override
	    public void loadingError(String errorMsg) {
	        // TODO Auto-generated method stub
	        
	    }
		
	    private String getFormPath (String instancePath){
	        // try to get the display name first
	        String selection1 = InstanceColumns.INSTANCE_FILE_PATH + 
	                                            " = "+"\"" +instancePath+"\"";
	        Cursor c1 = managedQuery(InstanceColumns.CONTENT_URI, null, selection1,
	                null, null);
	        c1.moveToFirst();
	        startManagingCursor(c1);
	        // get display name for querying
	        String instanceDisplayName = c1.getString(c1.getColumnIndex(InstanceColumns.DISPLAY_NAME));
	        
	        // edit selection to get only the form needed
	        String selection = FormsColumns.DISPLAY_NAME + " = " + "\"" +instanceDisplayName+"\"";
	         
	        Cursor c = managedQuery(FormsColumns.CONTENT_URI, null, selection,
	                null, null);
	        startManagingCursor(c);
	        c.moveToFirst();
	        return c.getString(c.getColumnIndex(FormsColumns.FORM_FILE_PATH));
	        
	    }
	

	  


}
