package com.sunner.imagesocket.Log;

/**
 * Created by sunner on 2016/4/8.
 */
public class Log {
    // Control Flag
    public static boolean enableVerbos = true;
    public static boolean enableInfo = true;
    public static boolean enableWarn = true;
    public static boolean enableError = true;


    public static void v(String TAG, String string) {
        if (enableVerbos)
            android.util.Log.v(TAG, string);
    }

    public static void i(String TAG, String string) {
        if (enableInfo)
            android.util.Log.i(TAG, string);
    }

    public static void w(String TAG, String string) {
        if (enableWarn)
            android.util.Log.w(TAG, string);
    }

    public static void e(String TAG, String string) {
        if (enableError)
            android.util.Log.e(TAG, string);
    }

    // Disable all log
    public static void shutUp(){
        enableError = enableInfo = enableWarn = enableVerbos = false;
    }

    // forbid the verbose log
    public static void normalDebug(){
        enableVerbos = false;
    }
}
