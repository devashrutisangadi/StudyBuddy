package com.example.studybuddy.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Persists and applies the user's manual dark/light mode choice.
 *
 * This is a MANUAL toggle independent of the system theme setting — the
 * app does NOT follow the device's system dark mode automatically. The
 * user's choice is stored in SharedPreferences and applied on every app
 * launch via BaseActivity.
 */
public class ThemePreferences {

    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_DARK_MODE_ENABLED = "dark_mode_enabled";

    private ThemePreferences() {
        // no instances
    }

    public static boolean isDarkModeEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_DARK_MODE_ENABLED, false);
    }

    public static void setDarkModeEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_DARK_MODE_ENABLED, enabled).apply();
        applyTheme(context);
    }

    /**
     * Applies the saved preference via AppCompatDelegate. Safe to call
     * repeatedly — AppCompatDelegate no-ops if the mode requested is
     * already the active mode.
     */
    public static void applyTheme(Context context) {
        boolean darkModeEnabled = isDarkModeEnabled(context);
        AppCompatDelegate.setDefaultNightMode(
                darkModeEnabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}