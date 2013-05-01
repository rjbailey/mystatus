package edu.washington.cs.mystatus.utilities;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import edu.washington.cs.mystatus.application.MyStatus;
import edu.washington.cs.mystatus.providers.FormsProviderAPI.FormTypes;
import edu.washington.cs.mystatus.providers.FormsProviderAPI.FormsColumns;

/**
 * PredicateSolver should parse and evaluate the predicates of all passive forms
 * which are not currently flagged as needing a response, and flag them if their
 * predicate evaluates to true or is null.
 * 
 * @author Jake Bailey (rjacob@cs.washington.edu)
 */
public class PredicateSolver {

    public static final String TAG = "PredicateSolver";

    /**
     * Evaluates the predicates of all passive forms currently flagged as not
     * needing a response. If a form's predicate evaluates to true (or is null
     * or invalid), flags the form as needing a response.
     */
    public static void evaluateAllPredicates() {
        String[] projection = new String[] {
                FormsColumns._ID,
                FormsColumns.DISPLAY_NAME,
                FormsColumns.PREDICATE,
                FormsColumns.LAST_RESPONSE
        };
        String selection = FormsColumns.NEEDS_RESPONSE + " = 0 AND "
                + FormsColumns.FORM_TYPE + " = ?";
        String[] selectionArgs = { Integer.toString(FormTypes.PASSIVE) };
        String sortOrder = FormsColumns._ID + " ASC";

        Cursor c = MyStatus.getInstance().getContentResolver()
                .query(FormsColumns.CONTENT_URI, projection, selection, selectionArgs, sortOrder);

        Log.d(TAG, "Found " + c.getCount() + " form(s) to be evaluated.");

        while (c.moveToNext()) {
            if (evaluatePredicate(c)) {
                // update needsResponse column in forms DB
                Uri uri = Uri.withAppendedPath(FormsColumns.CONTENT_URI, Integer.toString(c.getInt(0)));
                ContentValues values = new ContentValues();
                values.put(FormsColumns.NEEDS_RESPONSE, 1);
                MyStatus.getInstance().getContentResolver().update(uri, values, null, null);
            }
        }
        c.close();
    }

    /**
     * Evaluates a single predicate.
     * <p>
     * For now, a predicate is simply a Long value representing the number of
     * milliseconds that should have elapsed since the last response to the
     * survey.
     * <p>
     * Returns true for invalid predicates, but this may change in the future.
     * We should return true for null predicates (because those represent
     * unconditional surveys that may be responded to at any time), but when the
     * predicate has a syntax error or its input values are invalid, we may want
     * to do something else.
     * 
     * @param c A Cursor positioned on a row with projection (id, name,
     *        predicate, lastResponse).
     * @return If the predicate and its input values (e.g. lastResponse) are
     *         valid, returns the truth value of the evaluated predicate. If the
     *         predicate or its input values are invalid, returns true.
     */
    private static boolean evaluatePredicate(Cursor c) {
        int formId = c.getInt(0);
        String formName = c.getString(1);
        Log.d(TAG, "Evaluating predicate for Form #" + formId + ": " + formName);

        if (c.isNull(2) || c.isNull(3)) {
            if (c.isNull(2)) Log.i(TAG, "Predicate is null.");
            if (c.isNull(3)) Log.i(TAG, "The survey has never been filled out.");
            return true;
        }

        String predicate = c.getString(2);
        Long lastResponseTime = c.getLong(3);

        Long waitTime;
        try {
            waitTime = Long.parseLong(predicate);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid predicate: \"" + predicate + "\"");
            // always display passive forms with an invalid predicate
            return true;
        }
        Long now = Long.valueOf(System.currentTimeMillis());

        boolean result = (lastResponseTime + waitTime) < now;
        Log.i(TAG, "Predicate \"" + predicate + "\" evaluated to " + result);
        return result;
    }
}
