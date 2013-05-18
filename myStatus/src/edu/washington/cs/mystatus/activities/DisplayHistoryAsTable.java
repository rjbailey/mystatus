package edu.washington.cs.mystatus.activities;

import java.io.File;
import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import edu.washington.cs.mystatus.application.MyStatus;
import edu.washington.cs.mystatus.utilities.FileUtils;

public class DisplayHistoryAsTable extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		//FileUtils.parseXML(xmlFile);
		File f = new File (MyStatus.INSTANCES_PATH);
		if (f.isDirectory()){
			String [] folderList = f.list();
			for (String fn : folderList){
				File tempFolder = new File (fn);
				// safety check make sure it's a folder
				if (tempFolder.isDirectory()){
					String [] instanceList = tempFolder.list();
					for (String instance: instanceList){
						HashMap<String,String> elements = FileUtils.parseXML(new File(instance));
						String test = elements.toString();
					}
				}
				
			}
		}
		
	}
	
	
	
}
