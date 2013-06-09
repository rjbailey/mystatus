/**
 *  Emily Chien (eechien@cs.washington.edu)
 *  
 *  Database helper class to query, add and remove entries.
 */

package edu.washington.cs.mystatus.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class PrescriptionOpenHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DIR = Environment.getExternalStorageDirectory() + "myStatus/metadata";
	private static final String TAG = "myStatus.PrescriptionOpenHelper";
	
	private static final String DATABASE_NAME = "prescriptions.db";
	private static final String PRESCRIPTION_TABLE_NAME = "prescriptions";
	public static final String BRAND_NAME = "brandName";
    public static final String CHEMICAL_NAME = "chemicalName";
    public static final String PICTURE_FILENAME = "pictureFilename";
    public static final String HOUR = "hour";
    public static final String MINUTE = "minute";
    public static final String QUANTITY = "quantity";
    public static final String NOTIFICATION_ID = "notifId";
	
	
	public PrescriptionOpenHelper(Context context) {
		super(new ContextWrapper(context) {
			@Override
			public SQLiteDatabase openOrCreateDatabase(String name, int mode,
					SQLiteDatabase.CursorFactory factory) {
				SQLiteDatabase res = SQLiteDatabase.openDatabase(DIR + "/" + DATABASE_NAME, null,
						SQLiteDatabase.CREATE_IF_NECESSARY);
				return res;
			}
		}, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + PRESCRIPTION_TABLE_NAME + " ("
				+ "_id integer primary key, "
				+ BRAND_NAME + " text not null, "
				+ CHEMICAL_NAME + " text not null, "
				+ PICTURE_FILENAME + " text not null, "
				+ QUANTITY + " double not null, "
				+ HOUR + " integer not null, "
				+ MINUTE + " integer not null,"
				+ NOTIFICATION_ID + " integer not null);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// delete table and remake in full.
		Log.w(PrescriptionOpenHelper.class.getName(),
		        "Upgrading database from version " + oldVersion + " to "
		            + newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS");
		onCreate(db);
	}
	
	/**
	 * Adds a new row to the database that equals a new reminder.
	 */
	public void addNewPrescriptionNotification(String brandName, String chemName, String filename,
			double quantity, int hour, int min, int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(BRAND_NAME, brandName);
		values.put(CHEMICAL_NAME, chemName);
		values.put(QUANTITY, quantity);
		values.put(HOUR, hour);
		values.put(MINUTE, min);
		values.put(PICTURE_FILENAME, filename);
		values.put(NOTIFICATION_ID, id);
		
		if (db.insert(PRESCRIPTION_TABLE_NAME, null, values) == -1)
			Log.e(TAG, "Did not insert.");
		db.close();
	}
	
	/**
	 * Queries the table for all of the brand and chemical names
	 * @return a list of brand name followed by its chemical name, and repeat
	 */
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
	
	/**
	 * @return the total number of rows in the table
	 */
	public int getTotalCount() {
		String countQuery = "SELECT * FROM " + PRESCRIPTION_TABLE_NAME;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(countQuery, null);
		int count = c.getCount();
		c.close();
		return count;
	}
	
	/**
	 * Gets all of the Quantity and Time pairs for reminders based on the
	 * brand name and chemical name passed.
	 */
	public List<Double> getQuantTime(String brandName, String chemName) {
		List<Double> list= new ArrayList<Double>();
		SQLiteDatabase db = this.getReadableDatabase();
		String[] columns = {PrescriptionOpenHelper.HOUR,
				PrescriptionOpenHelper.MINUTE,
				PrescriptionOpenHelper.QUANTITY};
		String selection = PrescriptionOpenHelper.BRAND_NAME + "=?" + " AND " +
				PrescriptionOpenHelper.CHEMICAL_NAME + "=?";
		String[] selectionArgs = {brandName, chemName};
		Cursor c = db.query(PRESCRIPTION_TABLE_NAME, columns, selection, selectionArgs, null, null, null);
		if (c != null)
			c.moveToPosition(-1);
		while (c.moveToNext()) {
			double hour = c.getInt(0);
			double min = c.getInt(1);
			double quant = c.getDouble(2);
			list.add(hour);
			list.add(min);
			list.add(quant);
		}
		return list;
	}
	
	/**
	 * Delete all entries with the brand name and chemical name passed.
	 */
	public void deleteEntries(String brandName, String chemName) {
		SQLiteDatabase db = this.getWritableDatabase();
		String whereClause = BRAND_NAME + " = ? AND " + CHEMICAL_NAME + " = ?";;
		String[] whereArgs = {brandName, chemName};
		db.delete(PRESCRIPTION_TABLE_NAME, whereClause, whereArgs);
		db.close();
	}
}
