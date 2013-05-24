package edu.washington.cs.mystatus.providers;

import edu.washington.cs.mystatus.application.MyStatus;
import android.net.Uri;
import android.provider.BaseColumns;

public class PrescriptionProviderAPI {
	public static final String AUTHORITY = "edu.washington.cs.mystatus.provider.odk.prescriptions";
	
	private PrescriptionProviderAPI() {}
	
	public static final class PrescriptionColumns implements BaseColumns {
		private PrescriptionColumns() {}
		
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/prescriptions");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.odk.prescription";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.odk.prescription";
        
        public static final String PRESCRIPTION_FILE_PATH = "prescriptionFilePath";
        //public static final String JRCACHE_PRESCRIPTION_PATH = "jrcachePrescriptionPath";
        
        public static final String BRAND_NAME = "brandName";
        public static final String CHEMICAL_NAME = "chemicalName";
        public static final String PICTURE_FILENAME = "pictureFilename";
        public static final String HOUR = "hour";
        public static final String MINUTE = "minute";
        public static final String QUANTITY = "quantity";
	}
}
