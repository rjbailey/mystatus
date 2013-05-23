package edu.washington.cs.mystatus.database;

import java.io.File;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class PrescriptionOpenHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DIR = Environment.getExternalStorageDirectory() + "myStatus/metadata";
	
	private static final String PRESCRIPTION_TABLE_NAME = "prescriptions";
	//public static final String _ID = "_id";
	public static final String BRAND_NAME = "brandName";
    public static final String CHEMICAL_NAME = "chemicalName";
    public static final String PICTURE_FILENAME = "pictureFilename";
    public static final String HOUR = "hour";
    public static final String MINUTE = "minute";
    public static final String QUANTITY = "quantity";
	
	
	public PrescriptionOpenHelper(Context context) {
		//super(context, "prescriptions.db", null, DATABASE_VERSION);
		super(new ContextWrapper(context) {
			@Override
			public SQLiteDatabase openOrCreateDatabase(String name, int mode,
					SQLiteDatabase.CursorFactory factory) {
				return SQLiteDatabase.openDatabase(DIR + "/" + PRESCRIPTION_TABLE_NAME, null,
						SQLiteDatabase.CREATE_IF_NECESSARY);
			}
		}, PRESCRIPTION_TABLE_NAME, null, DATABASE_VERSION);
		//this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + PRESCRIPTION_TABLE_NAME + " ("
				+ "_id integer primary key, " // TODO: decide if need this
				+ BRAND_NAME + " text not null, "
				+ CHEMICAL_NAME + " text not null, "
				+ PICTURE_FILENAME + " text not null, "
				+ QUANTITY + " double not null, "
				+ HOUR + " integer not null, "
				+ MINUTE + " integer not null );");
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
