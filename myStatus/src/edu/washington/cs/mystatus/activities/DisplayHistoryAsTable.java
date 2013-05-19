package edu.washington.cs.mystatus.activities;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.LinearLayout;
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
import edu.washington.cs.mystatus.utilities.FileUtils;

public class DisplayHistoryAsTable extends Activity implements FormLoaderListener{
	 TableLayout tbl;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table_layout_list);
		tbl = (TableLayout)findViewById(R.id.dataTableLayout);
		//FileUtils.parseXML(xmlFile);
		File f = new File (MyStatus.INSTANCES_PATH);
		if (f.isDirectory()){
			String [] folderList = f.list();
			//for (String fn : folderList){
				File tempFolder = new File (f.getAbsolutePath()+"/"+folderList[0]);
				// safety check make sure it's a folder
				if (tempFolder.isDirectory()){
					String [] instanceList = tempFolder.list();
					//for (String instance: instanceList){
//						HashMap<String,String> elements = FileUtils.parseXML
//						                                    (new File(tempFolder.getAbsolutePath()+
//						                                                    "/"+instance));
						String instancePath = tempFolder.getAbsolutePath()+"/"+instanceList[0];
						FormLoaderTask loaderTask = new FormLoaderTask(instancePath, null, null);
						loaderTask.setFormLoaderListener(this);
						String formPath = getFormPath(instancePath);
						loaderTask.execute(formPath);	
						//String test = elements.toString();
					//}
				//}
				
			}
		}
		
	}
	
	 @Override
	    public void loadingComplete(FormLoaderTask task) {
	        FormController formController = task.getFormController();
	        MyStatus.getInstance().setFormController(formController);
	        ArrayList<HierarchyElement> elements = FileUtils.getHierarchyElements();     
	        ArrayList<String> title = new ArrayList<String>();
	        ArrayList<String> values = new ArrayList<String>();
	        // clean up some data
	        for(int i = 0; i < elements.size(); i++){
	            HierarchyElement el = elements.get(i);
	            if (el.getSecondaryText() != null){
	              //elements.remove(i);   
	                // create table view for it
	                title.add(el.getPrimaryText());
	                values.add(el.getSecondaryText());
	                
	            }
	        }
	        LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,
	                LayoutParams.WRAP_CONTENT);
	        TableRow titleRow = new TableRow (this);
	        for (int j = 0; j < title.size(); j ++){
	            TextView tv = new TextView (this);
	            tv.setText(title.get(j));
	            titleRow.addView(tv, params);     
	        }
	        tbl.addView(titleRow);
	        
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
