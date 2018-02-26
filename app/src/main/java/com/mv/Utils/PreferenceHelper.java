package com.mv.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * Created by acer on 8/9/2017.
 */

public class PreferenceHelper {
    private static final String PREFER_NAME = "MV";
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    public static final String TEMPLATENAME = "templatename";
    public static final String TEMPLATEID = "templateid";
    public static final String COMMUNITYID = "communityid";
    public static final String TOKEN = "Token";
    public static final String CONTENTISSYNCHED = "CONTENTISSYNCHED";

    public static final String FIRSTTIME_V_2_7 = "FIRSTTIME_V_2_7";

    public static final String APICALLTIME = "APICALLTIME";

    public static final String NOTIFICATION = "notification";
    public static final String MOBILEAPPVERSION = "mobileappversion";
    public static final String UserData = "UserData";

    public static final String AccessToken = "AccessToken";
    public static final String InstanceUrl = "InstanceUrl";
    public static final String SalesforceUserId = "SalesforceUserId";
    public static final String SalesforceUsername = "SalesforceUsername";
    public static final String SalesforcePassword = "SalesforcePassword";

    public PreferenceHelper(Context cntx) {
        this.context = cntx;
        pref = context.getSharedPreferences(PREFER_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void insertString(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public void insertInt(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    public void insertBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void insetLong(String key, long value) {
        editor.putLong(key, value);
        editor.commit();
    }

    public String getString(String key) {
        return pref.getString(key, "");
    }


    public int getInt(String key) {
        return pref.getInt(key, 0);
    }

    public boolean getBoolean(String key) {
        return pref.getBoolean(key, true);
    }

    public long getLong(String key) {
        return pref.getLong(key, 0);
    }

   /* public String getLongString(String key) {
        return String.valueOf(Long.valueOf(pref.getString(key, "")));
    }*/

    public void clearPrefrences() {
        Map<String, ?> prefs = pref.getAll();
        for (Map.Entry<String, ?> prefToReset : prefs.entrySet()) {
            if (prefToReset.getKey().equalsIgnoreCase(PreferenceHelper.TOKEN)
                    || prefToReset.getKey().equalsIgnoreCase(PreferenceHelper.InstanceUrl)) {
            } else {
                editor.remove(prefToReset.getKey()).commit();
            }

        }
    }

    public void clearPrefrences(String key) {
        editor.remove(key).commit();
    }

}
