package com.example.countify;

import android.annotation.SuppressLint;
import android.content.Context;

public class Shared {
    static final String USERDATA = "user_info";
    static final String TOKEN = "token";
    static final String TOKENIV = "token_iv";
    static final String SONGDATA = "song_data";
    static final String COUNTIFY = "countify";
    static final String TAG = "Countify_";

    static void saveInPersistence(Context context, String key, String value) {
        context.getSharedPreferences(COUNTIFY, 0)
                .edit()
                .putString(key, value)
                .apply();
    }
    @SuppressLint("ApplySharedPref")
    static void saveInPersistenceNow(Context context, String key, String value) {
        context.getSharedPreferences(COUNTIFY, 0)
                .edit()
                .putString(key, value)
                .commit();
    }

    static String getFromPersistence(Context context, String key) {
        return context.getSharedPreferences(COUNTIFY, 0)
                .getString(key, "");
    }
}
