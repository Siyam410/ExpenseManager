package com.example.expensemanager.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.expensemanager.data.local.TransactionEntity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Manager class for importing and exporting transaction data as JSON backup
 */
public class BackupManager {

    private static final String TAG = "BackupManager";
    private final Gson gson;

    public BackupManager() {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    /**
     * Export transactions to JSON format
     * @param transactions List of transactions to export
     * @return JSON string representation
     */
    public String exportToJson(List<TransactionEntity> transactions) {
        try {
            BackupData backupData = new BackupData();
            backupData.exportDate = System.currentTimeMillis();
            backupData.version = 1;
            backupData.transactionCount = transactions.size();
            backupData.transactions = transactions;

            return gson.toJson(backupData);
        } catch (Exception e) {
            Log.e(TAG, "Error exporting to JSON", e);
            return null;
        }
    }

    /**
     * Write JSON backup to a file URI
     * @param context Application context
     * @param uri URI of the file to write to
     * @param jsonData JSON string to write
     * @return true if successful, false otherwise
     */
    public boolean writeJsonToFile(Context context, Uri uri, String jsonData) {
        try {
            OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                outputStream.write(jsonData.getBytes());
                outputStream.close();
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error writing JSON to file", e);
        }
        return false;
    }

    /**
     * Import transactions from JSON file
     * @param context Application context
     * @param uri URI of the file to read from
     * @return List of imported transactions, or null if error
     */
    public List<TransactionEntity> importFromJson(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder jsonBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }

                reader.close();
                inputStream.close();

                String jsonData = jsonBuilder.toString();
                return parseJsonToTransactions(jsonData);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error importing from JSON", e);
        }
        return null;
    }

    /**
     * Parse JSON string to list of transactions
     * @param jsonData JSON string
     * @return List of transactions
     */
    private List<TransactionEntity> parseJsonToTransactions(String jsonData) {
        try {
            BackupData backupData = gson.fromJson(jsonData, BackupData.class);
            if (backupData != null && backupData.transactions != null) {
                Log.d(TAG, "Imported " + backupData.transactionCount + " transactions from backup dated " +
                      new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                          .format(new Date(backupData.exportDate)));
                return backupData.transactions;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing JSON", e);
        }
        return null;
    }

    /**
     * Generate default backup filename with timestamp
     * @return Filename string
     */
    public static String generateBackupFilename() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        return "expense_backup_" + sdf.format(new Date()) + ".json";
    }

    /**
     * Data class to wrap backup information
     */
    private static class BackupData {
        long exportDate;
        int version;
        int transactionCount;
        List<TransactionEntity> transactions;
    }
}

