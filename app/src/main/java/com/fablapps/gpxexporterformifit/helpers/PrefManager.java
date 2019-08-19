package com.fablapps.gpxexporterformifit.helpers;

/*
  Created by fablApps on 24.11.2017.
*/

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

public class PrefManager {

    private static SharedPreferences preferences;

    public PrefManager(@NonNull Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    public static void putInt(String key, int value) {
        preferences.edit().putInt(key, value).apply();
    }

    public static void putDouble(String key, double value) {
        preferences.edit().putLong(key, Double.doubleToRawLongBits(value)).apply();
    }

    public static void putString(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }

    public static void putBoolean(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    public static int getInt(String key) {
        return preferences.getInt(key, 0);
    }

    public static double getDouble(String key) {
        return Double.longBitsToDouble(preferences.getLong(key, 0));
    }

    public static String getString(String key) {
        return preferences.getString(key, null);
    }

    public static Set<String> getStringSet(String key) {
        return preferences.getStringSet(key, new HashSet<>());
    }

    public static boolean getBoolean(String key) {
        return preferences.getBoolean(key, false);
    }

    public static void removeKey(String key) {
        preferences.edit().remove(key).apply();
    }

    public static void clearPref() {
        preferences.edit().clear().apply();
    }
}