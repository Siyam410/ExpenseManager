package com.example.expensemanager.utils;

import android.graphics.Color;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for managing category colors
 */
public class CategoryColors {

    private static final Map<String, Integer> CATEGORY_COLOR_MAP = new HashMap<>();

    static {
        // Define unique colors for each category
        CATEGORY_COLOR_MAP.put("Food", Color.rgb(76, 175, 80));       // Green
        CATEGORY_COLOR_MAP.put("Transport", Color.rgb(255, 152, 0));  // Orange
        CATEGORY_COLOR_MAP.put("Shopping", Color.rgb(156, 39, 176));  // Purple
        CATEGORY_COLOR_MAP.put("Bills", Color.rgb(33, 150, 243));     // Blue
        CATEGORY_COLOR_MAP.put("Health", Color.rgb(0, 150, 136));     // Teal
        CATEGORY_COLOR_MAP.put("Education", Color.rgb(255, 235, 59)); // Yellow
        CATEGORY_COLOR_MAP.put("Entertainment", Color.rgb(244, 67, 54)); // Red
        CATEGORY_COLOR_MAP.put("Others", Color.rgb(158, 158, 158));   // Gray
        CATEGORY_COLOR_MAP.put("Groceries", Color.rgb(139, 195, 74)); // Light Green
        CATEGORY_COLOR_MAP.put("Utilities", Color.rgb(3, 169, 244));  // Light Blue
        CATEGORY_COLOR_MAP.put("Rent", Color.rgb(121, 85, 72));       // Brown
        CATEGORY_COLOR_MAP.put("Insurance", Color.rgb(63, 81, 181));  // Indigo
        CATEGORY_COLOR_MAP.put("Gifts", Color.rgb(233, 30, 99));      // Pink
        CATEGORY_COLOR_MAP.put("Travel", Color.rgb(255, 193, 7));     // Amber
        CATEGORY_COLOR_MAP.put("Fitness", Color.rgb(205, 220, 57));   // Lime
    }

    /**
     * Get color for a specific category
     * @param category Category name
     * @return Color integer
     */
    public static int getColorForCategory(String category) {
        if (category == null || category.isEmpty()) {
            return CATEGORY_COLOR_MAP.get("Others");
        }

        // Try exact match first
        if (CATEGORY_COLOR_MAP.containsKey(category)) {
            return CATEGORY_COLOR_MAP.get(category);
        }

        // Try case-insensitive match
        for (Map.Entry<String, Integer> entry : CATEGORY_COLOR_MAP.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(category)) {
                return entry.getValue();
            }
        }

        // Default to "Others" color
        return CATEGORY_COLOR_MAP.get("Others");
    }

    /**
     * Get a lighter version of the category color for backgrounds
     * @param category Category name
     * @return Light color integer
     */
    public static int getLightColorForCategory(String category) {
        int color = getColorForCategory(category);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        // Make it lighter (add 100 to each component, cap at 255)
        r = Math.min(255, r + 100);
        g = Math.min(255, g + 100);
        b = Math.min(255, b + 100);

        return Color.rgb(r, g, b);
    }
}

