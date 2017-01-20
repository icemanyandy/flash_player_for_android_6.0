package org.videolan.vlc.util;

import android.util.Log;


/**
 * Created by Administrator on 2016/10/12/012.
 */

public class LogUtils {
    private static final boolean debug = true;//BuildConfig.DEBUG;

    public static void i(String tag, String msg) {
        if (debug)
            Log.i(tag, msg);
    }

    public static void i(String msg) {
        if (debug) Log.i("vlc", msg);
    }
}
