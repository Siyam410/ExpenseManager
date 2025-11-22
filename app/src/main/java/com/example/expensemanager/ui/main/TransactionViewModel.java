package com.example.expensemanager.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.expensemanager.data.TransactionRepository;
import com.example.expensemanager.data.local.TransactionEntity;
//import com.example.expensemanager.utils.CloudBackupManager;  // HIDDEN - Work in Progress
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class TransactionViewModel extends AndroidViewModel {

    private final TransactionRepository repository;
    private final MutableLiveData<String> currentUserId = new MutableLiveData<>();
    private final LiveData<List<TransactionEntity>> allTransactions;
    //private final CloudBackupManager cloudBackupManager;  // HIDDEN - Work in Progress
    private boolean autoBackupEnabled = false;  // Disabled for now

    public TransactionViewModel(@NonNull Application application) {
        super(application);
        repository = new TransactionRepository(application);
        //cloudBackupManager = new CloudBackupManager();  // HIDDEN - Work in Progress

        // Get current user ID
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId.setValue(user.getUid());
        }

        // Switch LiveData based on current user
        allTransactions = Transformations.switchMap(currentUserId, userId -> {
            if (userId != null && !userId.isEmpty()) {
                return repository.getTransactionsByUser(userId);
            } else {
                return new MutableLiveData<>(new ArrayList<>());
            }
        });

        // Setup auto-backup listener (HIDDEN - Work in Progress)
        // TODO: Uncomment when ready to enable cloud backup
        /*
        repository.setTransactionChangeListener(() -> {
            if (autoBackupEnabled) {
                performAutoBackup();
            }
        });
        */
    }

    /* HIDDEN - Work in Progress
    private void performAutoBackup() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // Fetch current transactions and backup
        repository.getAllTransactionsSync(user.getUid(), transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                cloudBackupManager.autoBackupToCloud(transactions, new CloudBackupManager.OnCloudBackupListener() {
                    @Override
                    public void onBackupSuccess(int transactionCount) {
                        android.util.Log.d("TransactionViewModel", "Auto-backup: " + transactionCount + " transactions");
                    }

                    @Override
                    public void onBackupFailed(String error) {
                        android.util.Log.e("TransactionViewModel", "Auto-backup failed: " + error);
                    }
                });
            }
        });
    }
    */

    public LiveData<List<TransactionEntity>> getAllTransactions() {
        return allTransactions;
    }

    public void insert(TransactionEntity entity) {
        // Set userId before inserting
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            entity.userId = user.getUid();
        }
        repository.insert(entity);
    }

    public void update(TransactionEntity entity) {
        repository.update(entity);
    }

    public void delete(TransactionEntity entity) {
        repository.delete(entity);
    }

    public TransactionEntity getById(long id) {
        return repository.getById(id);
    }

    public void refreshUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId.setValue(user.getUid());
        }
    }

    public LiveData<List<TransactionEntity>> getTransactionsByMonth(String year, String month) {
        return Transformations.switchMap(currentUserId, userId -> {
            if (userId != null && !userId.isEmpty()) {
                return repository.getTransactionsByUserAndMonth(userId, year, month);
            } else {
                return new MutableLiveData<>(new ArrayList<>());
            }
        });
    }

    public TransactionRepository getRepository() {
        return repository;
    }

    /* HIDDEN - Work in Progress
    public CloudBackupManager getCloudBackupManager() {
        return cloudBackupManager;
    }
    */

    public void setAutoBackupEnabled(boolean enabled) {
        this.autoBackupEnabled = enabled;
    }

    public boolean isAutoBackupEnabled() {
        return autoBackupEnabled;
    }
}
