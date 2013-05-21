/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.washington.cs.mystatus.providers;

import info.guardianproject.cacheword.CacheWordActivityHandler;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import edu.washington.cs.mystatus.R;

import edu.washington.cs.mystatus.application.MyStatus;
import edu.washington.cs.mystatus.database.ODKSQLiteOpenHelper;
import edu.washington.cs.mystatus.providers.FormsProviderAPI.FormsColumns;
import edu.washington.cs.mystatus.providers.FormsProviderAPI.FormTypes;
import edu.washington.cs.mystatus.utilities.FileUtils;
import edu.washington.cs.mystatus.utilities.MediaUtils;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 *
 */
public class FormsProvider extends ContentProvider {

    private static final String t = "FormsProvider";

    private static final String DATABASE_NAME = "forms.db";
    private static final int DATABASE_VERSION = 5;
    private static final String FORMS_TABLE_NAME = "forms";
    // used for reset dB if neccessary
    // @CD
    private static final String RESET_DATABASE = "resetDb";
    
    private static HashMap<String, String> sFormsProjectionMap;

    private static final int FORMS = 1;
    private static final int FORM_ID = 2;
    
    private static final UriMatcher sUriMatcher;
    
    
    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends ODKSQLiteOpenHelper {
        // These exist in database versions 2 and 3, but not in 4...
        private static final String TEMP_FORMS_TABLE_NAME = "forms_v5";
        private static final String MODEL_VERSION = "modelVersion";

        DatabaseHelper(String databaseName, Context ctx) {
            super(MyStatus.METADATA_PATH, databaseName, null, DATABASE_VERSION, ctx);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
        	onCreateNamed(db, FORMS_TABLE_NAME);
        }

        private void onCreateNamed(SQLiteDatabase db, String tableName) {
            db.execSQL("CREATE TABLE " + tableName + " ("
            		+ FormsColumns._ID + " integer primary key, "
            		+ FormsColumns.DISPLAY_NAME + " text not null, "
                    + FormsColumns.DISPLAY_SUBTEXT + " text not null, "
                    + FormsColumns.DESCRIPTION + " text, "
                    + FormsColumns.JR_FORM_ID + " text not null, "
                    + FormsColumns.JR_VERSION + " text, "
                    + FormsColumns.MD5_HASH + " text not null, "
                    + FormsColumns.DATE + " integer not null, " // milliseconds
                    + FormsColumns.FORM_MEDIA_PATH + " text not null, "
                    + FormsColumns.FORM_FILE_PATH + " text not null, "
                    + FormsColumns.LANGUAGE + " text, "
                    + FormsColumns.SUBMISSION_URI + " text, "
                    + FormsColumns.BASE64_RSA_PUBLIC_KEY + " text, "
                    + FormsColumns.JRCACHE_FILE_PATH + " text not null, "
                    + FormsColumns.LAST_RESPONSE + " integer, " // milliseconds (date)
                    + FormsColumns.FORM_TYPE + " integer not null, "
                    + FormsColumns.PREDICATE + " text, "
                    + FormsColumns.NEEDS_RESPONSE + " integer not null );"); // boolean (0 or 1)
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	int initialVersion = oldVersion;
        	if ( oldVersion < 2 ) {
                Log.w(t, "Upgrading database from version " + oldVersion + " to " + newVersion
                        + ", which will destroy all old data");
                db.execSQL("DROP TABLE IF EXISTS " + FORMS_TABLE_NAME);
                onCreate(db);
                return;
        	} else {
        		// adding BASE64_RSA_PUBLIC_KEY and changing type and name of integer MODEL_VERSION to text VERSION
                db.execSQL("DROP TABLE IF EXISTS " + TEMP_FORMS_TABLE_NAME);
                onCreateNamed(db, TEMP_FORMS_TABLE_NAME);
        		db.execSQL("INSERT INTO " + TEMP_FORMS_TABLE_NAME + " ("
                		+ FormsColumns._ID + ", "
                		+ FormsColumns.DISPLAY_NAME + ", "
                        + FormsColumns.DISPLAY_SUBTEXT + ", "
                        + FormsColumns.DESCRIPTION + ", "
                        + FormsColumns.JR_FORM_ID + ", "
                        + FormsColumns.MD5_HASH + ", "
                        + FormsColumns.DATE + ", " // milliseconds
                        + FormsColumns.FORM_MEDIA_PATH + ", "
                        + FormsColumns.FORM_FILE_PATH + ", "
                        + FormsColumns.LANGUAGE + ", "
                        + FormsColumns.SUBMISSION_URI + ", "
                        + FormsColumns.JR_VERSION + ", "
                        + ((oldVersion < 3) ? "" : (FormsColumns.BASE64_RSA_PUBLIC_KEY + ", "))
                        + FormsColumns.JRCACHE_FILE_PATH + ", "
                        + ((oldVersion < 5) ? "" : (FormsColumns.LAST_RESPONSE + ", "))
                        + FormsColumns.FORM_TYPE + ", "
                        + ((oldVersion < 5) ? "" : (FormsColumns.PREDICATE + ", "))
                        + FormsColumns.NEEDS_RESPONSE
                        + ") SELECT "
                		+ FormsColumns._ID + ", "
                		+ FormsColumns.DISPLAY_NAME + ", "
                        + FormsColumns.DISPLAY_SUBTEXT + ", "
                        + FormsColumns.DESCRIPTION + ", "
                        + FormsColumns.JR_FORM_ID + ", "
                        + FormsColumns.MD5_HASH + ", "
                        + FormsColumns.DATE + ", " // milliseconds
                        + FormsColumns.FORM_MEDIA_PATH + ", "
                        + FormsColumns.FORM_FILE_PATH + ", "
                        + FormsColumns.LANGUAGE + ", "
                        + FormsColumns.SUBMISSION_URI + ", "
                        + ((oldVersion < 4) ? ("CASE WHEN " + MODEL_VERSION + " IS NOT NULL THEN " +
                        			"CAST(" + MODEL_VERSION + " AS TEXT) ELSE NULL END, ") : "NULL, ")
                        + ((oldVersion < 3) ? "" : (FormsColumns.BASE64_RSA_PUBLIC_KEY + ", "))
                        + FormsColumns.JRCACHE_FILE_PATH + ", "
                        + ((oldVersion < 5) ? "" : (FormsColumns.LAST_RESPONSE + ", "))
                        + ((oldVersion < 5) ? FormTypes.PASSIVE : FormsColumns.FORM_TYPE) + ", "
                        + ((oldVersion < 5) ? "" : (FormsColumns.PREDICATE + ", "))
                        + ((oldVersion < 5) ? "1" : FormsColumns.NEEDS_RESPONSE)
                        + " FROM " + FORMS_TABLE_NAME);

        		// risky failures here...
        		db.execSQL("DROP TABLE IF EXISTS " + FORMS_TABLE_NAME);
        		onCreateNamed(db, FORMS_TABLE_NAME);
        		db.execSQL("INSERT INTO " + FORMS_TABLE_NAME + " ("
                		+ FormsColumns._ID + ", "
                		+ FormsColumns.DISPLAY_NAME + ", "
                        + FormsColumns.DISPLAY_SUBTEXT + ", "
                        + FormsColumns.DESCRIPTION + ", "
                        + FormsColumns.JR_FORM_ID + ", "
                        + FormsColumns.MD5_HASH + ", "
                        + FormsColumns.DATE + ", " // milliseconds
                        + FormsColumns.FORM_MEDIA_PATH + ", "
                        + FormsColumns.FORM_FILE_PATH + ", "
                        + FormsColumns.LANGUAGE + ", "
                        + FormsColumns.SUBMISSION_URI + ", "
                        + FormsColumns.JR_VERSION + ", "
                        + FormsColumns.BASE64_RSA_PUBLIC_KEY + ", "
                        + FormsColumns.JRCACHE_FILE_PATH + ", "
                        + FormsColumns.LAST_RESPONSE + ", "
                        + FormsColumns.FORM_TYPE + ", "
                        + FormsColumns.PREDICATE + ", "
                        + FormsColumns.NEEDS_RESPONSE
                        + ") SELECT "
                		+ FormsColumns._ID + ", "
                		+ FormsColumns.DISPLAY_NAME + ", "
                        + FormsColumns.DISPLAY_SUBTEXT + ", "
                        + FormsColumns.DESCRIPTION + ", "
                        + FormsColumns.JR_FORM_ID + ", "
                        + FormsColumns.MD5_HASH + ", "
                        + FormsColumns.DATE + ", " // milliseconds
                        + FormsColumns.FORM_MEDIA_PATH + ", "
                        + FormsColumns.FORM_FILE_PATH + ", "
                        + FormsColumns.LANGUAGE + ", "
                        + FormsColumns.SUBMISSION_URI + ", "
                        + FormsColumns.JR_VERSION + ", "
                        + FormsColumns.BASE64_RSA_PUBLIC_KEY + ", "
                        + FormsColumns.JRCACHE_FILE_PATH + ", "
                        + FormsColumns.LAST_RESPONSE + ", "
                        + FormsColumns.FORM_TYPE + ", "
                        + FormsColumns.PREDICATE + ", "
                        + FormsColumns.NEEDS_RESPONSE
                        + " FROM " + TEMP_FORMS_TABLE_NAME);
        		db.execSQL("DROP TABLE IF EXISTS " + TEMP_FORMS_TABLE_NAME);

	            Log.w(t, "Successfully upgraded database from version " + initialVersion + " to " + newVersion
	                    + ", without destroying all the old data");
        	}
        }
    }

    private DatabaseHelper mDbHelper;


    @Override
    public boolean onCreate() {

        // must be at the beginning of any activity that can be called from an external intent
        MyStatus.createODKDirs();

        mDbHelper = new DatabaseHelper(DATABASE_NAME, this.getContext());
        
        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(FORMS_TABLE_NAME);

        switch (sUriMatcher.match(uri)) {
            case FORMS:
                qb.setProjectionMap(sFormsProjectionMap);
                break;

            case FORM_ID:
                qb.setProjectionMap(sFormsProjectionMap);
                qb.appendWhere(FormsColumns._ID + "=" + uri.getPathSegments().get(1));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Get the database and run the query
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }


    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case FORMS:
                return FormsColumns.CONTENT_TYPE;

            case FORM_ID:
                return FormsColumns.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }


    @Override
    public synchronized Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != FORMS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        if (!values.containsKey(FormsColumns.FORM_FILE_PATH)) {
        	throw new IllegalArgumentException(FormsColumns.FORM_FILE_PATH + " must be specified.");
        }

        // Normalize the file path.
        // (don't trust the requester).
        String filePath = values.getAsString(FormsColumns.FORM_FILE_PATH);
        File form = new File(filePath);
        filePath = form.getAbsolutePath(); // normalized
        values.put(FormsColumns.FORM_FILE_PATH, filePath);

        Long now = Long.valueOf(System.currentTimeMillis());

        // Make sure that the necessary fields are all set
        if (values.containsKey(FormsColumns.DATE) == false) {
            values.put(FormsColumns.DATE, now);
        }

        if (values.containsKey(FormsColumns.DISPLAY_SUBTEXT) == false) {
            Date today = new Date();
            String ts = new SimpleDateFormat(getContext().getString(R.string.added_on_date_at_time), Locale.getDefault()).format(today);
            values.put(FormsColumns.DISPLAY_SUBTEXT, ts);
        }

        if (values.containsKey(FormsColumns.DISPLAY_NAME) == false) {
            values.put(FormsColumns.DISPLAY_NAME, form.getName());
        }

        // don't let users put in a manual md5 hash
        if (values.containsKey(FormsColumns.MD5_HASH)) {
            values.remove(FormsColumns.MD5_HASH);
        }
        String md5 = FileUtils.getMd5Hash(form);
        values.put(FormsColumns.MD5_HASH, md5);

        if (values.containsKey(FormsColumns.JRCACHE_FILE_PATH) == false) {
            String cachePath = MyStatus.CACHE_PATH + File.separator + md5 + ".formdef";
            values.put(FormsColumns.JRCACHE_FILE_PATH, cachePath);
        }
        if (values.containsKey(FormsColumns.FORM_MEDIA_PATH) == false) {
            String pathNoExtension = filePath.substring(0, filePath.lastIndexOf("."));
            String mediaPath = pathNoExtension + "-media";
            values.put(FormsColumns.FORM_MEDIA_PATH, mediaPath);
        }

        // make sure we have a valid form type
        Integer formType = values.getAsInteger(FormsColumns.FORM_TYPE);
        if (formType == null ||
                (formType != FormTypes.PASSIVE && formType != FormTypes.TRIGGERED)) {
            values.put(FormsColumns.FORM_TYPE, FormTypes.PASSIVE);
        }
        // by default, set needsResponse to true for new forms
        Integer needsResponse = values.getAsInteger(FormsColumns.NEEDS_RESPONSE);
        if (needsResponse == null || (needsResponse != 0 && needsResponse != 1)) {
            values.put(FormsColumns.NEEDS_RESPONSE, 1);
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // first try to see if a record with this filename already exists...
        String[] projection = {
                FormsColumns._ID, FormsColumns.FORM_FILE_PATH
        };
        String[] selectionArgs = { filePath };
        String selection = FormsColumns.FORM_FILE_PATH + "=?";
        Cursor c = null;
        try {
        	c = db.query(FORMS_TABLE_NAME, projection, selection, selectionArgs, null, null, null);
        	if ( c.getCount() > 0 ) {
        		// already exists
        		throw new SQLException("FAILED Insert into " + uri + " -- row already exists for form definition file: " + filePath);
        	}
        } finally {
        	if ( c != null ) {
        		c.close();
        	}
        }

        long rowId = db.insert(FORMS_TABLE_NAME, null, values);
        if (rowId > 0) {
            Uri formUri = ContentUris.withAppendedId(FormsColumns.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(formUri, null);
        	MyStatus.getInstance().getActivityLogger().logActionParam(this, "insert",
        			formUri.toString(),  values.getAsString(FormsColumns.FORM_FILE_PATH));
            return formUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }


    private void deleteFileOrDir(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            if (file.isDirectory()) {
            	// delete any media entries for files in this directory...
                int images = MediaUtils.deleteImagesInFolderFromMediaProvider(file);
                int audio = MediaUtils.deleteAudioInFolderFromMediaProvider(file);
                int video = MediaUtils.deleteVideoInFolderFromMediaProvider(file);

                Log.i(t, "removed from content providers: " + images
                        + " image files, " + audio + " audio files,"
                        + " and " + video + " video files.");

                // delete all the containing files
                File[] files = file.listFiles();
                for (File f : files) {
                    // should make this recursive if we get worried about
                    // the media directory containing directories
                    Log.i(t, "attempting to delete file: " + f.getAbsolutePath());
                    f.delete();
                }
            }
            file.delete();
            Log.i(t, "attempting to delete file: " + file.getAbsolutePath());
        }
    }


    /**
     * This method removes the entry from the content provider, and also removes any associated
     * files. files: form.xml, [formmd5].formdef, formname-media {directory}
     */
    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count;

        switch (sUriMatcher.match(uri)) {
            case FORMS:
                Cursor del = null;
                try {
                	del = this.query(uri, null, where, whereArgs, null);
	                del.moveToPosition(-1);
	                while (del.moveToNext()) {
	                    deleteFileOrDir(del.getString(del
	                            .getColumnIndex(FormsColumns.JRCACHE_FILE_PATH)));
	                    String formFilePath = del.getString(del.getColumnIndex(FormsColumns.FORM_FILE_PATH));
	                    MyStatus.getInstance().getActivityLogger().logAction(this, "delete", formFilePath);
	                    deleteFileOrDir(formFilePath);
	                    deleteFileOrDir(del.getString(del.getColumnIndex(FormsColumns.FORM_MEDIA_PATH)));
	                }
                } finally {
                	if ( del != null ) {
                		del.close();
                	}
                }
                count = db.delete(FORMS_TABLE_NAME, where, whereArgs);
                break;

            case FORM_ID:
                String formId = uri.getPathSegments().get(1);

                Cursor c = null;
                try {
                	c = this.query(uri, null, where, whereArgs, null);
	                // This should only ever return 1 record.
	                c.moveToPosition(-1);
	                while (c.moveToNext()) {
	                    deleteFileOrDir(c.getString(c.getColumnIndex(FormsColumns.JRCACHE_FILE_PATH)));
	                    String formFilePath = c.getString(c.getColumnIndex(FormsColumns.FORM_FILE_PATH));
	                    MyStatus.getInstance().getActivityLogger().logAction(this, "delete", formFilePath);
	                    deleteFileOrDir(formFilePath);
	                    deleteFileOrDir(c.getString(c.getColumnIndex(FormsColumns.FORM_MEDIA_PATH)));
	                }
                } finally {
                	if ( c != null ) {
                		c.close();
                	}
                }

                count =
                    db.delete(FORMS_TABLE_NAME,
                        FormsColumns._ID + "=" + formId
                                + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""),
                        whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }


    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
    	// adding some trick to reset database at first login as well as 
    	// keep supporting for older api
    	// @CD
    	if ((values == null) && (where.equals(RESET_DATABASE)) && (whereArgs == null)){
    		resetDatabase();
    		return 0;
    	}
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case FORMS:
                // don't let users manually update md5
                if (values.containsKey(FormsColumns.MD5_HASH)) {
                    values.remove(FormsColumns.MD5_HASH);
                }
                // if values contains path, then all filepaths and md5s will get updated
                // this probably isn't a great thing to do.
                if (values.containsKey(FormsColumns.FORM_FILE_PATH)) {
                    String formFile = values.getAsString(FormsColumns.FORM_FILE_PATH);
                    values.put(FormsColumns.MD5_HASH, FileUtils.getMd5Hash(new File(formFile)));
                }

                Cursor c = null;
                try {
                	c = this.query(uri, null, where, whereArgs, null);

	                if (c.getCount() > 0) {
	                    c.moveToPosition(-1);
	                    while (c.moveToNext()) {
	                        // before updating the paths, delete all the files
	                        if (values.containsKey(FormsColumns.FORM_FILE_PATH)) {
	                            String newFile = values.getAsString(FormsColumns.FORM_FILE_PATH);
	                            String delFile =
	                                c.getString(c.getColumnIndex(FormsColumns.FORM_FILE_PATH));
	                            if (newFile.equalsIgnoreCase(delFile)) {
	                                // same file, so don't delete anything
	                            } else {
	                                // different files, delete the old one
	                                deleteFileOrDir(delFile);
	                            }

	                            // either way, delete the old cache because we'll calculate a new one.
	                            deleteFileOrDir(c.getString(c
	                                    .getColumnIndex(FormsColumns.JRCACHE_FILE_PATH)));
	                        }
	                    }
	                }
                } finally {
                	if ( c != null ) {
                		c.close();
                	}
                }

                // Make sure that the necessary fields are all set
                if (values.containsKey(FormsColumns.DATE) == true) {
                    Date today = new Date();
                    String ts = new SimpleDateFormat(getContext().getString(R.string.added_on_date_at_time), Locale.getDefault()).format(today);
                    values.put(FormsColumns.DISPLAY_SUBTEXT, ts);
                }

                count = db.update(FORMS_TABLE_NAME, values, where, whereArgs);
                break;

            case FORM_ID:
                String formId = uri.getPathSegments().get(1);
                // Whenever file paths are updated, delete the old files.

                Cursor update = null;
                try {
                	update = this.query(uri, null, where, whereArgs, null);

	                // This should only ever return 1 record.
	                if (update.getCount() > 0) {
	                    update.moveToFirst();

	                    // don't let users manually update md5
	                    if (values.containsKey(FormsColumns.MD5_HASH)) {
	                        values.remove(FormsColumns.MD5_HASH);
	                    }

	                    // the order here is important (jrcache needs to be before form file)
	                    // because we update the jrcache file if there's a new form file
	                    if (values.containsKey(FormsColumns.JRCACHE_FILE_PATH)) {
	                        deleteFileOrDir(update.getString(update
	                                .getColumnIndex(FormsColumns.JRCACHE_FILE_PATH)));
	                    }

	                    if (values.containsKey(FormsColumns.FORM_FILE_PATH)) {
	                        String formFile = values.getAsString(FormsColumns.FORM_FILE_PATH);
	                        String oldFile =
	                            update.getString(update.getColumnIndex(FormsColumns.FORM_FILE_PATH));

	                        if (formFile != null && formFile.equalsIgnoreCase(oldFile)) {
	                            // Files are the same, so we may have just copied over something we had
	                            // already
	                        } else {
	                            // New file name. This probably won't ever happen, though.
	                            deleteFileOrDir(oldFile);
	                        }

	                        // we're updating our file, so update the md5
	                        // and get rid of the cache (doesn't harm anything)
	                        deleteFileOrDir(update.getString(update
	                                .getColumnIndex(FormsColumns.JRCACHE_FILE_PATH)));
	                        String newMd5 = FileUtils.getMd5Hash(new File(formFile));
	                        values.put(FormsColumns.MD5_HASH, newMd5);
	                        values.put(FormsColumns.JRCACHE_FILE_PATH,
	                        		MyStatus.CACHE_PATH + File.separator + newMd5 + ".formdef");
	                    }

	                    // Make sure that the necessary fields are all set
	                    if (values.containsKey(FormsColumns.DATE) == true) {
	                        Date today = new Date();
	                        String ts =
	                            new SimpleDateFormat(getContext().getString(R.string.added_on_date_at_time), Locale.getDefault()).format(today);
	                        values.put(FormsColumns.DISPLAY_SUBTEXT, ts);
	                    }

	                    count =
	                        db.update(FORMS_TABLE_NAME, values, FormsColumns._ID + "=" + formId
	                                + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""),
	                            whereArgs);
	                } else {
	                    Log.e(t, "Attempting to update row that does not exist");
	                }
                } finally {
                	if ( update != null ) {
                		update.close();
                	}
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(FormsProviderAPI.AUTHORITY, "forms", FORMS);
        sUriMatcher.addURI(FormsProviderAPI.AUTHORITY, "forms/#", FORM_ID);
        
        sFormsProjectionMap = new HashMap<String, String>();
        sFormsProjectionMap.put(FormsColumns._ID, FormsColumns._ID);
        sFormsProjectionMap.put(FormsColumns.DISPLAY_NAME, FormsColumns.DISPLAY_NAME);
        sFormsProjectionMap.put(FormsColumns.DISPLAY_SUBTEXT, FormsColumns.DISPLAY_SUBTEXT);
        sFormsProjectionMap.put(FormsColumns.DESCRIPTION, FormsColumns.DESCRIPTION);
        sFormsProjectionMap.put(FormsColumns.JR_FORM_ID, FormsColumns.JR_FORM_ID);
        sFormsProjectionMap.put(FormsColumns.JR_VERSION, FormsColumns.JR_VERSION);
        sFormsProjectionMap.put(FormsColumns.SUBMISSION_URI, FormsColumns.SUBMISSION_URI);
        sFormsProjectionMap.put(FormsColumns.BASE64_RSA_PUBLIC_KEY, FormsColumns.BASE64_RSA_PUBLIC_KEY);
        sFormsProjectionMap.put(FormsColumns.MD5_HASH, FormsColumns.MD5_HASH);
        sFormsProjectionMap.put(FormsColumns.DATE, FormsColumns.DATE);
        sFormsProjectionMap.put(FormsColumns.FORM_MEDIA_PATH, FormsColumns.FORM_MEDIA_PATH);
        sFormsProjectionMap.put(FormsColumns.FORM_FILE_PATH, FormsColumns.FORM_FILE_PATH);
        sFormsProjectionMap.put(FormsColumns.JRCACHE_FILE_PATH, FormsColumns.JRCACHE_FILE_PATH);
        sFormsProjectionMap.put(FormsColumns.LANGUAGE, FormsColumns.LANGUAGE);
        sFormsProjectionMap.put(FormsColumns.LAST_RESPONSE, FormsColumns.LAST_RESPONSE);
        sFormsProjectionMap.put(FormsColumns.FORM_TYPE, FormsColumns.FORM_TYPE);
        sFormsProjectionMap.put(FormsColumns.PREDICATE, FormsColumns.PREDICATE);
        sFormsProjectionMap.put(FormsColumns.NEEDS_RESPONSE, FormsColumns.NEEDS_RESPONSE);
    }
    // reset database used for first initiaized
    // @CD
    public void resetDatabase() {
        mDbHelper.close();
        mDbHelper = new DatabaseHelper(DATABASE_NAME, this.getContext());
    }

}
