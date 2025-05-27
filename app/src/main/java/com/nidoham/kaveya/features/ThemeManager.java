package com.nidoham.kaveya.features;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Utility class for managing app themes (light/dark mode)
 * Created for 2025 technology upgrade
 */
public class ThemeManager {
    
    private static ThemeManager instance;
    private final Context context;
    private final SharedPreferences preferences;
    
    private static final String PREF_NAME = "theme_preferences";
    private static final String KEY_THEME_MODE = "theme_mode";
    
    // Theme modes
    public static final int MODE_SYSTEM = 0;
    public static final int MODE_LIGHT = 1;
    public static final int MODE_DARK = 2;
    
    private ThemeManager(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized ThemeManager getInstance(Context context) {
        if (instance == null) {
            instance = new ThemeManager(context);
        }
        return instance;
    }
    
    /**
     * Apply the saved theme mode
     */
    public void applyTheme() {
        int themeMode = getThemeMode();
        
        switch (themeMode) {
            case MODE_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case MODE_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case MODE_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
    
    /**
     * Set the theme mode
     */
    public void setThemeMode(int themeMode) {
        preferences.edit().putInt(KEY_THEME_MODE, themeMode).apply();
        applyTheme();
    }
    
    /**
     * Get the current theme mode
     */
    public int getThemeMode() {
        return preferences.getInt(KEY_THEME_MODE, MODE_SYSTEM);
    }
    
    /**
     * Check if dark mode is currently active
     */
    public boolean isDarkModeActive() {
        int currentNightMode = context.getResources().getConfiguration().uiMode 
                & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }
    
    /**
     * Toggle between light and dark mode
     */
    public void toggleDarkMode() {
        if (isDarkModeActive()) {
            setThemeMode(MODE_LIGHT);
        } else {
            setThemeMode(MODE_DARK);
        }
    }
}
