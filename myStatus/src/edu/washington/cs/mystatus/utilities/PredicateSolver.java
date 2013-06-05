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
                FormsColumns.LAST_RESPONSE,
                FormsColumns.DATE
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
     * Valid predicates include:
     * <ul>
     * <li>The number of milliseconds that need to have elapsed since the last
     * response to the survey.</li>
     * <li>The string "daysSinceLastResponse:N", where N is the number of days
     * that need to have elapsed since the last response to the survey.</li>
     * <li>The string "onceOnly", which indicates that the survey only ever
     * needs a single response, and will not be displayed after the user has
     * responded to it.</li>
     * <li>The string "daysDelayed:N", where N is the number of days that need
     * to have elapsed since the survey was downloaded before it is made visible
     * to the user. Delayed surveys currently only ever accept one response
     * (like onceOnly surveys).</li>
     * </ul>
     * Returns true for invalid predicates, but this may change in the future.
     * We should return true for null predicates (because those represent
     * unconditional surveys that may be responded to at any time), but when the
     * predicate has a syntax error or its input values are invalid, we may want
     * to do something else.
     * 
     * @param c A Cursor positioned on a row with projection (id, name,
     *        predicate, lastResponse, date).
     * @return If the predicate and its input values (e.g. lastResponse) are
     *         valid, returns the truth value of the evaluated predicate. If the
     *         predicate or its input values are invalid, returns true.
     */
    private static boolean evaluatePredicate(Cursor c) {
        // TODO: Each predicate should be extracted into its own method.

        int formId = c.getInt(0);
        String formName = c.getString(1);
        Log.d(TAG, "Evaluating predicate for Form #" + formId + ": " + formName);

        if (c.isNull(2)) {
            Log.i(TAG, "Predicate is null.");
            return true;
        }

        String predicate = c.getString(2);

        if (c.isNull(3)) {
            Log.i(TAG, "The survey has never been filled out.");

            if (predicate.startsWith("daysDelayed:")) {
                // Delayed surveys only need a response if it's been at least
                // N days since the survey was downloaded.

                Long now = Long.valueOf(System.currentTimeMillis());
                Long subscriptionDate = c.getLong(4);

                long daysToDelay;
                try {
                    daysToDelay = Long.valueOf(predicate.split(":")[1]);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid predicate: \"" + predicate + "\"");
                    return true;
                }

                long millisPerDay = 24 * 60 * 60 * 1000;
                long daysElapsed = (now - subscriptionDate) / millisPerDay;

                boolean result = daysElapsed >= daysToDelay;
                Log.i(TAG, "Predicate \"" + predicate + "\" evaluated to " + result);
                return result;
            }
            // All other surveys need a response if they've never had one.
            return true;
        }

        if (predicate.equals("onceOnly")) {
            Log.i(TAG, "The survey is a once-only survey which has been filled out.");
            return false;
        }

        Long now = Long.valueOf(System.currentTimeMillis());
        Long lastResponseTime = c.getLong(3);
        
        if (predicate.startsWith("daysSinceLastResponse:")) {
            long daysInPeriod;
            try {
                daysInPeriod = Long.valueOf(predicate.split(":")[1]);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid predicate: \"" + predicate + "\"");
                return true;
            }
            
            long millisPerDay = 24 * 60 * 60 * 1000;
            long daysElapsed = (now - lastResponseTime) / millisPerDay;
            
            boolean result = daysElapsed >= daysInPeriod;
            Log.i(TAG, "Predicate \"" + predicate + "\" evaluated to " + result);
            return result;
        }

        // simple number-of-milliseconds predicate
        Long waitTime;
        try {
            waitTime = Long.parseLong(predicate);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid predicate: \"" + predicate + "\"");
            // always display passive forms with an invalid predicate
            return true;
        }

        boolean result = (lastResponseTime + waitTime) < now;
        Log.i(TAG, "Predicate \"" + predicate + "\" evaluated to " + result);
        return result;
    }
}
