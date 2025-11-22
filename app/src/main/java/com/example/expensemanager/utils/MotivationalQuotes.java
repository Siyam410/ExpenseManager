package com.example.expensemanager.utils;

import java.util.Random;

/**
 * Utility class for generating motivational quotes
 */
public class MotivationalQuotes {

    private static final String[] QUOTES = {
            "Great progress! Small savings create big results.",
            "You're doing amazing! Keep your spending balanced.",
            "Every taka saved today is freedom tomorrow.",
            "You're in control — keep going!",
            "Your financial discipline is inspiring!",
            "Consistency is the key to financial success!",
            "Smart spending today, brighter tomorrow!",
            "You're building a better financial future!",
            "Keep tracking, keep saving, keep thriving!",
            "Your dedication to budgeting is admirable!",
            "Small steps lead to big financial wins!",
            "You're making wise choices with your money!",
            "Financial freedom starts with awareness!",
            "Every tracked expense is a step forward!",
            "You're on the right path to financial wellness!",
            "Great job managing your finances!",
            "Your future self will thank you!",
            "Discipline today, prosperity tomorrow!",
            "You're creating healthy money habits!",
            "Keep up the excellent work!"
    };

    private static final Random random = new Random();

    /**
     * Get a random motivational quote
     * @return A random motivational quote string
     */
    public static String getRandomQuote() {
        int index = random.nextInt(QUOTES.length);
        return QUOTES[index];
    }

    /**
     * Get all available quotes
     * @return Array of all quotes
     */
    public static String[] getAllQuotes() {
        return QUOTES.clone();
    }
}

