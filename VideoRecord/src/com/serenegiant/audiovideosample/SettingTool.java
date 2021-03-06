package com.serenegiant.audiovideosample;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.UUID;

public class SettingTool {

    static SettingTool whatthis = null;
    static Context mctx = null;
    static SharedPreferences settings;
    static SharedPreferences.Editor editor;

    public static final boolean DEF_SOFTBOYBY = false;

    public static SettingTool init(Context ctx) {
        if (ctx != null) {
            mctx = ctx;
        }
        if (whatthis == null) {
            whatthis = new SettingTool();

            settings = mctx.getSharedPreferences("settings", 0);
            editor = settings.edit();
            initFirstRun();
        }
        return whatthis;
    }

    final static int HOURS = 3600;

    public static void initFirstRun() {
        long lasthours = settings.getLong("seconds", -1);
        if (lasthours == -1) {
            long millis = System.currentTimeMillis();
            long seconds = millis / 1000;
            Log.e("yangdi", "settings first save time " + seconds);
            editor.putLong("seconds", seconds);

            //使用默认资源。
            editor.putString("livephoto_path_img", "default_picture");
            editor.putString("livephoto_path_video", "default_video");
            editor.commit();
        }

        //generateId();
    }

    public static String generateId() {
        String lastid = settings.getString("generateId", "");
        if (TextUtils.isEmpty(lastid)) {
            TelephonyManager tm = (TelephonyManager) mctx.getSystemService(Context.TELEPHONY_SERVICE);
            String id = tm.getDeviceId();
            if (TextUtils.isEmpty(id) || id.contains("000000")) {
                String oder = UUID.randomUUID().toString().replaceAll("-", "");
                if (oder != null && oder.length() > 8) {
                    oder = oder.substring(0, 8);
                }
                id = oder;
            }
            editor.putString("generateId", id);
            editor.commit();
            return id;
        } else {

        }
        return lastid;

    }

    public static boolean getFirstRunTimeOut(int offSeconds) {
        long lasthours = settings.getLong("seconds", -1);
        long millis = System.currentTimeMillis();
        long seconds = millis / 1000;
        if (lasthours == -1) {
            Log.e("yangdi", "getFirstRunTimeOut error don't save time before");
            return true;
        } else {
            Log.e("yangdi", "getFirstRunTimeOut at time " + offSeconds);
            return (lasthours + offSeconds) < seconds;
        }
    }

    public static boolean getSecondRunTimeOut(int offSeconds, boolean clean) {
        long lasthours = settings.getLong("offseconds", -1);
        long millis = System.currentTimeMillis();
        long seconds = millis / 1000;
        if (lasthours == -1 || clean) {
            editor.putLong("offseconds", seconds);
            editor.commit();
        }
        if (lasthours == -1) {
            Log.e("yangdi", "getSecondRunTimeOut first save time " + seconds);
            return true;
        } else {
            Log.e("yangdi", "getSecondRunTimeOut at time " + offSeconds);
            return (lasthours + offSeconds) < seconds;
        }
    }

    public static SettingTool getInstance() {
        return whatthis;
    }

    public static String CACHE_ALARMMODE;
    public static int CACHE_RECTAREA;
    public static int CACHE_RECTNUMBER;
    public static int CACHE_LOOPTIME;
    public static boolean CACHE_REMOTEMODE;
    public static boolean CACHE_MUSICMODE;
    public static boolean CACHE_LEDMODE;
    public static boolean CACHE_SOFTBOYBY;

    public static float CACHE_SK_W = 1.0f;
    public static float CACHE_SK_H = 1.0f;
    public static int SCREEN_W = 0;
    public static int SCREEN_H = 0;
    public static float SCREEN_DP = 0;
    public static String PASSWORD_KEY = "PASSWORD_KEY";
    public static String USE_CODE_LOCK_KEY = "USE_CODE_LOCK_KEY";

    public static boolean HIDE_PAY = false;

    public static void cache() {
        CACHE_SOFTBOYBY = getSoftboyBY();
    }

    public String toString() {
        StringBuilder e = new StringBuilder();

        e.append("getSoftboyBY:");
        e.append(getSoftboyBY());

        return e.toString();
    }

    public static boolean getSVIP(){
        return settings.getBoolean("DEF_SOFTBOYBY_SVIP", false);
    }

    public static void setSVIP(boolean svip){
        editor.putBoolean("DEF_SOFTBOYBY_SVIP", svip);
        editor.commit();
    }

    public static boolean getSoftboyBY() {
        return settings.getBoolean("DEF_SOFTBOYBY", DEF_SOFTBOYBY);
    }

    public static boolean getSoftboyBY_HIDE_PAY() {
        return settings.getBoolean("DEF_SOFTBOYBY", DEF_SOFTBOYBY) || HIDE_PAY;
    }

    public static void setSoftboyBY(boolean b) {
        editor.putBoolean("DEF_SOFTBOYBY", b);
        editor.commit();
    }

    public static void setData(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public static String getData(String key, String defValue) {
        return settings.getString(key, defValue);
    }

    public static void setData(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean getData(String key, boolean defValue) {
        return settings.getBoolean(key, defValue);
    }

    public static void setData(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getData(String key, int defValue) {
        return settings.getInt(key, defValue);
    }
}
