package edu.washington.cs.mystatus.providers;

import java.io.File;
import java.util.HashMap;

import edu.washington.cs.mystatus.application.MyStatus;
import edu.washington.cs.mystatus.database.ODKSQLiteOpenHelper;
import edu.washington.cs.mystatus.providers.FormsProviderAPI.FormsColumns;
import edu.washington.cs.mystatus.providers.PrescriptionProviderAPI.PrescriptionColumns;
import edu.washington.cs.mystatus.utilities.MediaUtils;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class PrescriptionProvider extends ContentProvider {

	private static final String TAG = "PrescriptionProvider";
	
	private static final String DATABASE_NAME = "prescriptions.db";
    private static final int DATABASE_VERSION = 1;
    private static final String PRESCRIPTIONS_TABLE_NAME = "prescriptions";
    
    private static HashMap<String, String> sPrescriptionsProjectionMap;

    private static final int PRESCRIPTIONS = 1;
    private static final int PRESCRIPTION_ID = 2;

    private static UriMatcher sUriMatcher; // final???
    
    private DatabaseHelper mDbHelper;
	
	private static class DatabaseHelper extends ODKSQLiteOpenHelper {
		
		DatabaseHelper(String databaseName) {
			super(MyStatus.METADATA_PATH, databaseName, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + PRESCRIPTIONS_TABLE_NAME + " ("
					+ PrescriptionColumns._ID + " integer primary key, "
					+ PrescriptionColumns.BRAND_NAME + " text not null, "
					+ PrescriptionColumns.CHEMICAL_NAME + " text not null, "
					+ PrescriptionColumns.PICTURE_FILENAME + " text not null, "
					+ PrescriptionColumns.QUANTITY + " double not null, "
					+ PrescriptionColumns.HOUR + " integer not null, "
					+ PrescriptionColumns.MINUTE + " integer not null; )");
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(DatabaseHelper.class.getName(),
			        "Upgrading database from version " + oldVersion + " to "
			            + newVersion + ", which will destroy all old data");
			    db.execSQL("DROP TABLE IF EXISTS");
			    onCreate(db);
		}
	}
	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int count;
		
		switch (sUriMatcher.match(uri)) {
		case PRESCRIPTIONS:
			Cursor del = null;
			try {
				del = this.query(uri, null, where, whereArgs, null);
				del.moveToPosition(-1);
				while (del.moveToNext()) {
					String formFilePath = del.getString(del.getColumnIndex(PrescriptionColumns.PRESCRIPTION_FILE_PATH));
					MyStatus.getInstance().getActivityLogger().logAction(this, "delete", formFilePath);
					deleteFileOrDir(formFilePath);
					deleteFileOrDir(del.getString(del.getColumnIndex(PrescriptionColumns.PICTURE_FILENAME)));
				}
			} finally {
				if (del != null) {
					del.close();
				}
			}
			count = db.delete(PRESCRIPTIONS_TABLE_NAME, where, whereArgs);
			break;
		case PRESCRIPTION_ID:
			String formId = uri.getPathSegments().get(1);
			
			Cursor c = null;
			try {
				c = this.query(uri, null, where, whereArgs, null);
				
				c.moveToPosition(-1);
				while (c.moveToNext()) {
					String formFilePath = c.getString(c.getColumnIndex(PrescriptionColumns.PRESCRIPTION_FILE_PATH));
					MyStatus.getInstance().getActivityLogger().logAction(this, "delete", formFilePath);
					deleteFileOrDir(formFilePath);
					deleteFileOrDir(c.getString(c.getColumnIndex(PrescriptionColumns.PICTURE_FILENAME)));
				}
			} finally {
				if (c != null) {
					c.close();
				}
			}
			count = db.delete(PRESCRIPTIONS_TABLE_NAME,
					PrescriptionColumns._ID + "=" + formId + 
					(!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""),
					whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
			
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case PRESCRIPTIONS:
			return PrescriptionColumns.CONTENT_TYPE;
		case PRESCRIPTION_ID:
			return PrescriptionColumns.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (sUriMatcher.match(uri) != PRESCRIPTIONS) {
			throw new IllegalArgumentException("Unknown URI" + uri);
		}
		
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else{
			values = new ContentValues();
		}
		
		String filePath = values.getAsString(PrescriptionColumns.PRESCRIPTION_FILE_PATH);
		File form = new File(filePath);
		filePath = form.getAbsolutePath();
		values.put(PrescriptionColumns.PRESCRIPTION_FILE_PATH, filePath);
		
		/*if (values.containsKey(PrescriptionColumns.BRAND_NAME) == false) {
			
		}
		
		if (values.containsKey(PrescriptionColumns.CHEMICAL_NAME) == false) {
			
		}
		
		// check this?
		if (values.containsKey(PrescriptionColumns.PICTURE_FILENAME) == false) {
			
		}
		
		if (values.containsKey(PrescriptionColumns.QUANTITY) == false) {
			
		}
		
		if (values.containsKey(PrescriptionColumns.TIME) == false) {
			
		}*/
		
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		
		/*String[] projection = {
				PrescriptionColumns._ID, PrescriptionColumns.PRESCRIPTION_FILE_PATH
		};
		String[] selectionArgs = { filePath };
		String selection = PrescriptionColumns.PRESCRIPTION_FILE_PATH + "=?";
		Cursor c = null;
		try {
			c = db.query(PRESCRIPTIONS_TABLE_NAME, projection, selection, selectionArgs, null, null, null);
			if (c.getCount() > 0) {
				throw new SQLException("FAILED Insert into " + uri +
						" -- row already exists for prescription definitition file" + filePath);
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}*/
		
		long rowId = db.insert(PRESCRIPTIONS_TABLE_NAME, null, values);
		if (rowId > 0) {
			Uri prescriptionUri = ContentUris.withAppendedId(FormsColumns.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(prescriptionUri, null);
			MyStatus.getInstance().getActivityLogger().logActionParam(this, "insert", 
					prescriptionUri.toString(), values.getAsString(PrescriptionColumns.PRESCRIPTION_FILE_PATH));
			return prescriptionUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		MyStatus.createODKDirs();
		mDbHelper = new DatabaseHelper(DATABASE_NAME);
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(PRESCRIPTIONS_TABLE_NAME);
		
		switch (sUriMatcher.match(uri)) {
		case PRESCRIPTIONS:
			qb.setProjectionMap(sPrescriptionsProjectionMap);
			break;
		case PRESCRIPTION_ID:
			qb.setProjectionMap(sPrescriptionsProjectionMap);
			qb.appendWhere(PrescriptionColumns._ID + "=" + uri.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int count = 0;
		switch (sUriMatcher.match(uri)) {
			case PRESCRIPTIONS:
				/*Cursor c = null;
				try {
					c = this.query(uri, null, where, whereArgs, null);
					if (c.getCount() > 0) {
						c.moveToPosition(-1);
						while (c.moveToNext()) {
							if (values.containsKey(PrescriptionColumns.PRESCRIPTION_FILE_PATH)) {
								String newFile = values.getAsString(PrescriptionColumns.PRESCRIPTION_FILE_PATH);
								String delFile =
										c.getString(c.getColumnIndex(PrescriptionColumns.PRESCRIPTION_FILE_PATH));
								if(newFile.equalsIgnoreCase(delFile)) {
									
								} else {
									deleteFileOrDir(delFile);
								}
								deleteFileOrDir(c.getString(c
										.getColumnIndex(PrescriptionColumns.JRCACHE_PRESCRIPTION_PATH)));
							}
						}
					}
				} finally {
					if (c != null) {
						c.close();
					}
				}
				*/
				count = db.update(PRESCRIPTIONS_TABLE_NAME, values, where, whereArgs);
				break;
			case PRESCRIPTION_ID:
				String prescriptionId = uri.getPathSegments().get(1);
				/*Cursor update = null;
				try {
					update = this.query(uri, null, where, whereArgs, null);
					
					if (update.getCount() > 0) {
						update.moveToFirst();
						
						if (values.containsKey(PrescriptionColumns.JRCACHE_PRESCRIPTION_PATH)) {
							deleteFileOrDir(update.getString(update
									.getColumnIndex(PrescriptionColumns.JRCACHE_PRESCRIPTION_PATH)));
						}
						if (values.containsKey(PrescriptionColumns.PRESCRIPTION_FILE_PATH)) {
							String prescriptionFile = values.getAsString(PrescriptionColumns.PRESCRIPTION_FILE_PATH);
							String oldFile =
								update.getString(update.getColumnIndex(PrescriptionColumns.PRESCRIPTION_FILE_PATH));
							if (prescriptionFile != null && prescriptionFile.equalsIgnoreCase(oldFile)) {
								
							} else {
								deleteFileOrDir(oldFile);
							}
							
						}
					}
				} finally {
					
				}*/
				count = db.update(PRESCRIPTIONS_TABLE_NAME, values, PrescriptionColumns._ID
						+ "=" + prescriptionId + (!TextUtils.isEmpty(where) ? " AND (" +
						where + ')' : ""), whereArgs);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(PrescriptionProviderAPI.AUTHORITY, "prescriptions", PRESCRIPTIONS);
        sUriMatcher.addURI(PrescriptionProviderAPI.AUTHORITY, "prescriptions/#", PRESCRIPTION_ID);
		
        sPrescriptionsProjectionMap = new HashMap<String, String>();
        sPrescriptionsProjectionMap.put(PrescriptionColumns._ID, PrescriptionColumns._ID);
        sPrescriptionsProjectionMap.put(PrescriptionColumns.BRAND_NAME, PrescriptionColumns.BRAND_NAME);
        sPrescriptionsProjectionMap.put(PrescriptionColumns.CHEMICAL_NAME, PrescriptionColumns.CHEMICAL_NAME);
        sPrescriptionsProjectionMap.put(PrescriptionColumns.PICTURE_FILENAME, PrescriptionColumns.PICTURE_FILENAME);
        sPrescriptionsProjectionMap.put(PrescriptionColumns.QUANTITY, PrescriptionColumns.QUANTITY);
        sPrescriptionsProjectionMap.put(PrescriptionColumns.HOUR, PrescriptionColumns.HOUR);
        sPrescriptionsProjectionMap.put(PrescriptionColumns.MINUTE, PrescriptionColumns.MINUTE);
	}
	
	private void deleteFileOrDir(String filename) {
		File file = new File(filename);
		if (file.exists()) {
			if (file.isDirectory()) {
				int images = MediaUtils.deleteImagesInFolderFromMediaProvider(file);
				Log.i(TAG, "removed from content providers: " + images + " image files.");
				File[] files = file.listFiles();
				for (File f : files) {
					Log.i(TAG, "attemption to delete file: " + f.getAbsolutePath());
					f.delete();
				}
			}
			file.delete();
			Log.i(TAG, "attempting to delete file: " + file.getAbsolutePath());
		}
	}

}
