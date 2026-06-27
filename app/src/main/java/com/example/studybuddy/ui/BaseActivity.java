package com.example.studybuddy.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.studybuddy.utils.ThemePreferences;

/**
 * All Activities should extend this instead of AppCompatActivity directly.
 *
 * Handles two cross-cutting concerns so individual Activities don't have
 * to repeat them:
 *
 * 1. Applies the user's saved dark/light mode preference via
 *    AppCompatDelegate.setDefaultNightMode() before the Activity's own
 *    onCreate() runs, so the correct theme is active before
 *    setContentView() inflates any layout.
 *
 * 2. Applies system-bar (status bar / nav bar) inset padding to the root
 *    content view, so screen content doesn't render flush under the
 *    status bar. This was previously added ad-hoc per-Activity and got
 *    missed more than once (QuizActivity, QuizSummaryActivity,
 *    SettingsActivity all shipped without it at first) — centralizing it
 *    here means every future screen gets it automatically just by
 *    extending BaseActivity, with no extra code required in onCreate().
 *
 * Subclasses with their own IME-aware padding needs (e.g. a screen with
 * a text input that should resize above the keyboard) should still add
 * their own ViewCompat listener for that specific case, as ChatActivity/
 * AddNotesActivity do — this base behavior only covers system bars.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemePreferences.applyTheme(this);
        super.onCreate(savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}