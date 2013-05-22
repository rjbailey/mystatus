package edu.washington.cs.mystatus;

import net.sqlcipher.database.SQLiteDatabase;
import edu.washington.cs.mystatus.application.MyStatus;
import edu.washington.cs.mystatus.database.ODKSQLiteOpenHelper;
import android.content.Context;
import android.util.Log;

public class PrescriptionOpenHelper extends ODKSQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	
	private static final String PRESCRIPTION_TABLE_NAME = "prescriptions";
	public static final String _ID = "_id";
	public static final String BRAND_NAME = "brandName";
    public static final String CHEMICAL_NAME = "chemicalName";
    public static final String PICTURE_FILENAME = "pictureFilename";
    public static final String HOUR = "hour";
    public static final String MINUTE = "minute";
    public static final String QUANTITY = "quantity";
	
	
	public PrescriptionOpenHelper(String databaseName) {
		super(MyStatus.METADATA_PATH, databaseName, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + PRESCRIPTION_TABLE_NAME + " ("
				+ _ID + " integer not null, " // TODO: decide if need this
				+ BRAND_NAME + " text not null, "
				+ CHEMICAL_NAME + " text not null, "
				+ PICTURE_FILENAME + " text not null, "
				+ QUANTITY + " double not null, "
				+ HOUR + " integer not null, "
				+ MINUTE + " integer not null; )");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(PrescriptionOpenHelper.class.getName(),
		        "Upgrading database from version " + oldVersion + " to "
		            + newVersion + ", which will destroy all old data");
		    db.execSQL("DROP TABLE IF EXISTS");
		    onCreate(db);
	}
}
