package com.example.expensemanager.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.expensemanager.data.local.AppDatabase;
import com.example.expensemanager.data.local.TransactionDao;
import com.example.expensemanager.data.local.TransactionEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionRepository {

    private final TransactionDao transactionDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private OnTransactionChangeListener changeListener;

    public TransactionRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.transactionDao = db.transactionDao();
    }

    public void setTransactionChangeListener(OnTransactionChangeListener listener) {
        this.changeListener = listener;
    }

    public LiveData<List<TransactionEntity>> getAllTransactions() {
        return transactionDao.getAll();
    }

    public LiveData<List<TransactionEntity>> getTransactionsByUser(String userId) {
        return transactionDao.getAllByUser(userId);
    }

    public void insert(TransactionEntity entity) {
        executor.execute(() -> {
            transactionDao.insert(entity);
            notifyTransactionChange();
        });
    }

    public void update(TransactionEntity entity) {
        executor.execute(() -> {
            transactionDao.update(entity);
            notifyTransactionChange();
        });
    }

    public void delete(TransactionEntity entity) {
        executor.execute(() -> {
            transactionDao.delete(entity);
            notifyTransactionChange();
        });
    }

    private void notifyTransactionChange() {
        if (changeListener != null) {
            changeListener.onTransactionChanged();
        }
    }

    public TransactionEntity getById(long id) {
        return transactionDao.getById(id);
    }

    public LiveData<List<TransactionEntity>> getTransactionsByUserAndMonth(String userId, String year, String month) {
        return transactionDao.getTransactionsByUserAndMonth(userId, year, month);
    }

    public void insertAll(List<TransactionEntity> entities, OnCompleteListener listener) {
        executor.execute(() -> {
            try {
                for (TransactionEntity entity : entities) {
                    // Reset ID to let database auto-generate new IDs
                    entity.id = 0;
                    transactionDao.insert(entity);
                }
                if (listener != null) {
                    listener.onComplete(true, entities.size());
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onComplete(false, 0);
                }
            }
        });
    }

    public void getAllTransactionsSync(String userId, OnTransactionsFetchedListener listener) {
        executor.execute(() -> {
            List<TransactionEntity> transactions = transactionDao.getAllByUserSync(userId);
            if (listener != null) {
                listener.onTransactionsFetched(transactions);
            }
        });
    }

    public interface OnCompleteListener {
        void onComplete(boolean success, int count);
    }

    public interface OnTransactionsFetchedListener {
        void onTransactionsFetched(List<TransactionEntity> transactions);
    }

    public interface OnTransactionChangeListener {
        void onTransactionChanged();
    }
}
