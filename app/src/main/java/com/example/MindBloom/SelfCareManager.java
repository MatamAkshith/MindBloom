package com.example.MindBloom;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * SelfCareManager manages daily self-care checklist persistence.
 * Uses Android SharedPreferences scoped strictly to the authenticated user's UID:
 * "self_care_prefs_{userUid}"
 *
 * Implements automatic daily reset behavior by comparing current date with the last recorded date.
 */
public class SelfCareManager {

    private static final String PREF_PREFIX = "self_care_prefs_";
    private static final String KEY_LAST_DATE = "key_last_date";
    private static final String KEY_ITEM_PREFIX = "key_item_";
    public static final int TOTAL_ITEMS = 5;

    private final SharedPreferences prefs;

    public SelfCareManager(Context context, String userUid) {
        String safeUid = (userUid != null && !userUid.trim().isEmpty()) ? userUid : "guest";
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_PREFIX + safeUid, Context.MODE_PRIVATE);
        checkAndResetForNewDay();
    }

    /**
     * Checks if the date has changed since the last app usage.
     * If a new day is detected, resets all checklist items to unchecked.
     */
    public void checkAndResetForNewDay() {
        String today = getTodayDateKey();
        String lastDate = prefs.getString(KEY_LAST_DATE, null);

        if (lastDate == null || !lastDate.equals(today)) {
            SharedPreferences.Editor editor = prefs.edit();
            for (int i = 0; i < TOTAL_ITEMS; i++) {
                editor.putBoolean(KEY_ITEM_PREFIX + i, false);
            }
            editor.putString(KEY_LAST_DATE, today);
            editor.apply();
        }
    }

    /**
     * Mark an item as checked or unchecked.
     */
    public void setItemChecked(int index, boolean checked) {
        if (index < 0 || index >= TOTAL_ITEMS) return;
        prefs.edit().putBoolean(KEY_ITEM_PREFIX + index, checked).apply();
    }

    /**
     * Return whether an item is checked.
     */
    public boolean isItemChecked(int index) {
        if (index < 0 || index >= TOTAL_ITEMS) return false;
        return prefs.getBoolean(KEY_ITEM_PREFIX + index, false);
    }

    /**
     * Calculate how many checklist items have been checked.
     */
    public int getCompletedCount() {
        int count = 0;
        for (int i = 0; i < TOTAL_ITEMS; i++) {
            if (prefs.getBoolean(KEY_ITEM_PREFIX + i, false)) {
                count++;
            }
        }
        return count;
    }

    public int getTotalCount() {
        return TOTAL_ITEMS;
    }

    /**
     * Formatted date string for user-facing display (e.g. "Wednesday, 28 Oct").
     */
    public String getFormattedDisplayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMM", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String getTodayDateKey() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Manual reset for current day (useful for viva demonstration and testing).
     */
    public void resetChecklist() {
        SharedPreferences.Editor editor = prefs.edit();
        for (int i = 0; i < TOTAL_ITEMS; i++) {
            editor.putBoolean(KEY_ITEM_PREFIX + i, false);
        }
        editor.putString(KEY_LAST_DATE, getTodayDateKey());
        editor.apply();
    }
}
