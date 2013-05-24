package edu.washington.cs.mystatus.database;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.washington.cs.mystatus.activities.EditPrescription;
import edu.washington.cs.mystatus.activities.ManagePrescriptionActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

public class PrescriptionOpenHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DIR = Environment.getExternalStorageDirectory() + "myStatus/metadata";
	
	private static final String DATABASE_NAME = "prescriptions.db";
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
				SQLiteDatabase res = SQLiteDatabase.openDatabase(DIR + "/" + DATABASE_NAME, null,
						SQLiteDatabase.CREATE_IF_NECESSARY);
				return res;
			}
		}, DATABASE_NAME, null, DATABASE_VERSION);
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
	
	public void addNewPrescriptionNotification(String brandName, String chemName, String filename,
			double quantity, int hour, int min) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(PrescriptionOpenHelper.BRAND_NAME, brandName);
		values.put(PrescriptionOpenHelper.CHEMICAL_NAME, chemName);
		values.put(PrescriptionOpenHelper.QUANTITY, quantity);
		values.put(PrescriptionOpenHelper.HOUR, hour);
		values.put(PrescriptionOpenHelper.MINUTE, min);
		values.put(PrescriptionOpenHelper.PICTURE_FILENAME, filename);
		
		db.insert(PRESCRIPTION_TABLE_NAME, null, values);
		db.close();
	}
	
	public List<String> getBrandChemFilenames() {
		SQLiteDatabase db = this.getReadableDatabase();
		String[] columns = {PrescriptionOpenHelper.BRAND_NAME,
				PrescriptionOpenHelper.CHEMICAL_NAME,
				PrescriptionOpenHelper.PICTURE_FILENAME};
		Cursor c = db.query(PRESCRIPTION_TABLE_NAME, columns, null, null,
				PrescriptionOpenHelper.BRAND_NAME, null, null);
		if (c != null)
			c.moveToPosition(-1);
		List<String> names = new ArrayList<String>();
		while(c.moveToNext()) {
			String brandName = c.getString(0);
			names.add(brandName);
			String chemName = c.getString(1);
			names.add(chemName);
			String filename = c.getString(2);
			names.add(filename);
		}
		return names;
	}
	
	public int getTotalCount() {
		String countQuery = "SELECT * FROM " + PRESCRIPTION_TABLE_NAME;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(countQuery, null);
		int count = c.getCount();
		c.close();
		return count;
	}
}
