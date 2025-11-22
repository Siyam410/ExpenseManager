package com.example.expensemanager.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Utility class for managing monthly budget preferences
 */
public class BudgetPreferences {

    private static final String PREF_NAME = "BudgetPreferences";
    private static final String KEY_BUDGET_PREFIX = "monthly_budget_";

    /**
     * Save monthly budget for the current user
     * @param context Application context
     * @param budget Budget amount
     */
    public static void saveBudget(Context context, double budget) {
        String userId = getCurrentUserId();
        if (userId == null) return;

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putFloat(KEY_BUDGET_PREFIX + userId, (float) budget).apply();
    }

    /**
     * Get monthly budget for the current user
     * @param context Application context
     * @return Budget amount, or 0 if not set
     */
    public static double getBudget(Context context) {
        String userId = getCurrentUserId();
        if (userId == null) return 0.0;

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getFloat(KEY_BUDGET_PREFIX + userId, 0f);
    }

    /**
     * Check if budget is set for the current user
     * @param context Application context
     * @return true if budget is set, false otherwise
     */
    public static boolean isBudgetSet(Context context) {
        return getBudget(context) > 0;
    }

    /**
     * Clear budget for the current user
     * @param context Application context
     */
    public static void clearBudget(Context context) {
        String userId = getCurrentUserId();
        if (userId == null) return;

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_BUDGET_PREFIX + userId).apply();
    }

    /**
     * Get current user ID from Firebase Auth
     * @return User ID or null
     */
    private static String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : null;
    }
}

