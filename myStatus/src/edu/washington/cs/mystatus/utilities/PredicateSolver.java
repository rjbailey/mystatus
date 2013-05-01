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

    public static final String TAG = "mystatus.PredicateSolver";

    /**
     * Evaluates the predicates of all passive forms currently flagged as not
     * needing a response. If a form's predicate evaluates to true (or is null
     * or invalid), flags the form as needing a response.
     */
    public static void evaluateAllPredicates() {
        String[] projection = new String[] {
                FormsColumns._ID, FormsColumns.PREDICATE, FormsColumns.LAST_RESPONSE
        };
        String selection = FormsColumns.NEEDS_RESPONSE + " = 0 AND "
                + FormsColumns.FORM_TYPE + " = ?";
        String[] selectionArgs = { Integer.toString(FormTypes.PASSIVE) };
        String sortOrder = FormsColumns._ID + " ASC";

        Cursor c = MyStatus.getInstance().getContentResolver()
                .query(FormsColumns.CONTENT_URI, projection, selection, selectionArgs, sortOrder);

        while (c.moveToNext()) {
            if (evaluatePredicate(c)) {
                // update needsResponse column in forms DB
                Uri uri = Uri.withAppendedPath(FormsColumns.CONTENT_URI, Integer.toString(c.getInt(0)));
                ContentValues values = new ContentValues();
                values.put(FormsColumns.NEEDS_RESPONSE, 1);
                MyStatus.getInstance().getContentResolver().update(uri, values, null, null);
            }
        }
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
     * @param c A Cursor positioned on a row with projection (id, predicate,
     *        lastResponse).
     * @return If the predicate and its input values (e.g. lastResponse) are
     *         valid, returns the truth value of the evaluated predicate. If the
     *         predicate or its input values are invalid, returns true.
     */
    private static boolean evaluatePredicate(Cursor c) {
        // always display passive forms where the predicate is null
        // or the last response time is null (i.e. the form has never been filled out before)
        if (c.isNull(1) || c.isNull(2)) {
            return true;
        }

        int formId = c.getInt(0);
        String predicate = c.getString(1);
        Long lastResponseTime = c.getLong(2);

        Long waitTime;
        try {
            waitTime = Long.parseLong(predicate);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid predicate in form with ID " + formId + ": \"" + predicate + "\"");
            // always display passive forms with an invalid predicate
            return true;
        }
        Long now = Long.valueOf(System.currentTimeMillis());

        return (lastResponseTime + waitTime) < now;
    }
}
