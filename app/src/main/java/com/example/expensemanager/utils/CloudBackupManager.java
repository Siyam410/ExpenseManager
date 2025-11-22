package com.example.expensemanager.utils;

import android.content.Context;
import android.util.Log;

import com.example.expensemanager.data.local.TransactionEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Manager class for automatic cloud backup to Firebase Storage
 * Handles automatic backup after each transaction and one-click restore
 */
public class CloudBackupManager {

    private static final String TAG = "CloudBackupManager";
    private static final String BACKUP_FOLDER = "expense_backups";
    private static final String BACKUP_FILENAME = "auto_backup.json";

    private final Gson gson;
    private final FirebaseStorage storage;
    private final BackupManager localBackupManager;

    public CloudBackupManager() {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        storage = FirebaseStorage.getInstance();
        localBackupManager = new BackupManager();
    }

    /**
     * Get the current user's backup reference in Firebase Storage
     * Path: expense_backups/{userId}/auto_backup.json
     */
    private StorageReference getUserBackupReference() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return null;
        }

        String userId = user.getUid();
        return storage.getReference()
                .child(BACKUP_FOLDER)
                .child(userId)
                .child(BACKUP_FILENAME);
    }

    /**
     * Automatically backup transactions to Firebase Storage
     * Called after each transaction add/edit/delete
     */
    public void autoBackupToCloud(List<TransactionEntity> transactions, OnCloudBackupListener listener) {
        StorageReference backupRef = getUserBackupReference();
        if (backupRef == null) {
            Log.e(TAG, "User not authenticated");
            if (listener != null) {
                listener.onBackupFailed("User not authenticated");
            }
            return;
        }

        // Convert transactions to JSON
        String jsonData = localBackupManager.exportToJson(transactions);
        if (jsonData == null) {
            Log.e(TAG, "Failed to create JSON backup");
            if (listener != null) {
                listener.onBackupFailed("Failed to create backup");
            }
            return;
        }

        // Upload to Firebase Storage
        byte[] data = jsonData.getBytes();
        UploadTask uploadTask = backupRef.putBytes(data);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            Log.d(TAG, "Auto-backup successful: " + transactions.size() + " transactions");
            if (listener != null) {
                listener.onBackupSuccess(transactions.size());
            }
        }).addOnFailureListener(exception -> {
            Log.e(TAG, "Auto-backup failed", exception);
            if (listener != null) {
                listener.onBackupFailed(exception.getMessage());
            }
        });
    }

    /**
     * Restore transactions from Firebase Storage
     * One-click restore functionality
     */
    public void restoreFromCloud(OnCloudRestoreListener listener) {
        StorageReference backupRef = getUserBackupReference();
        if (backupRef == null) {
            Log.e(TAG, "User not authenticated");
            if (listener != null) {
                listener.onRestoreFailed("User not authenticated");
            }
            return;
        }

        // Check if backup exists
        backupRef.getMetadata().addOnSuccessListener(metadata -> {
            // Backup exists, download it
            long maxDownloadSize = 10 * 1024 * 1024; // 10MB max
            backupRef.getBytes(maxDownloadSize)
                .addOnSuccessListener(bytes -> {
                    try {
                        String jsonData = new String(bytes);
                        BackupData backupData = gson.fromJson(jsonData, BackupData.class);

                        if (backupData != null && backupData.transactions != null) {
                            Date backupDate = new Date(backupData.exportDate);
                            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                            String dateStr = sdf.format(backupDate);

                            Log.d(TAG, "Cloud restore successful: " + backupData.transactionCount + " transactions");
                            if (listener != null) {
                                listener.onRestoreSuccess(backupData.transactions, dateStr);
                            }
                        } else {
                            if (listener != null) {
                                listener.onRestoreFailed("Invalid backup data");
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing backup data", e);
                        if (listener != null) {
                            listener.onRestoreFailed("Failed to parse backup: " + e.getMessage());
                        }
                    }
                })
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Failed to download backup", exception);
                    if (listener != null) {
                        listener.onRestoreFailed("Download failed: " + exception.getMessage());
                    }
                });
        }).addOnFailureListener(exception -> {
            Log.e(TAG, "No cloud backup found", exception);
            if (listener != null) {
                listener.onRestoreFailed("No cloud backup found");
            }
        });
    }

    /**
     * Check if a cloud backup exists for the current user
     */
    public void checkBackupExists(OnBackupExistsListener listener) {
        StorageReference backupRef = getUserBackupReference();
        if (backupRef == null) {
            if (listener != null) {
                listener.onCheckComplete(false, null);
            }
            return;
        }

        backupRef.getMetadata()
            .addOnSuccessListener(metadata -> {
                Date lastModified = new Date(metadata.getUpdatedTimeMillis());
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                String dateStr = sdf.format(lastModified);

                if (listener != null) {
                    listener.onCheckComplete(true, dateStr);
                }
            })
            .addOnFailureListener(exception -> {
                if (listener != null) {
                    listener.onCheckComplete(false, null);
                }
            });
    }

    /**
     * Delete cloud backup for current user
     */
    public void deleteCloudBackup(OnDeleteListener listener) {
        StorageReference backupRef = getUserBackupReference();
        if (backupRef == null) {
            if (listener != null) {
                listener.onDeleteFailed("User not authenticated");
            }
            return;
        }

        backupRef.delete()
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Cloud backup deleted");
                if (listener != null) {
                    listener.onDeleteSuccess();
                }
            })
            .addOnFailureListener(exception -> {
                Log.e(TAG, "Failed to delete backup", exception);
                if (listener != null) {
                    listener.onDeleteFailed(exception.getMessage());
                }
            });
    }

    // Callback interfaces
    public interface OnCloudBackupListener {
        void onBackupSuccess(int transactionCount);
        void onBackupFailed(String error);
    }

    public interface OnCloudRestoreListener {
        void onRestoreSuccess(List<TransactionEntity> transactions, String backupDate);
        void onRestoreFailed(String error);
    }

    public interface OnBackupExistsListener {
        void onCheckComplete(boolean exists, String lastBackupDate);
    }

    public interface OnDeleteListener {
        void onDeleteSuccess();
        void onDeleteFailed(String error);
    }

    /**
     * Data class to wrap backup information (matches BackupManager format)
     */
    private static class BackupData {
        long exportDate;
        int version;
        int transactionCount;
        List<TransactionEntity> transactions;
    }
}

