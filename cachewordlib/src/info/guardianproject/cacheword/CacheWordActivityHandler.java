package info.guardianproject.cacheword;

import android.content.Context;

public class CacheWordActivityHandler extends CacheWordHandler {

    public CacheWordActivityHandler(Context context) {
        super(context);
    }

    /**
     * Call this method in your Activity's onResume()
     */
    public void onResume() {
        connectToService();
    }

    /**
     * Call this method in your Activity's onPause()
     */
    public void onPause() {
        disconnect();
    }

}
