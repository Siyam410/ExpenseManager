package com.example.expensemanager.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    long insert(TransactionEntity entity);

    @Update
    void update(TransactionEntity entity);

    @Delete
    void delete(TransactionEntity entity);

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    TransactionEntity getById(long id);

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY dateTimestamp DESC")
    LiveData<List<TransactionEntity>> getAllByUser(String userId);

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY dateTimestamp DESC")
    List<TransactionEntity> getAllByUserSync(String userId);

    @Query("SELECT * FROM transactions ORDER BY dateTimestamp DESC")
    LiveData<List<TransactionEntity>> getAll();

    @Query("SELECT * FROM transactions WHERE userId = :userId AND strftime('%Y', datetime(dateTimestamp/1000, 'unixepoch')) = :year AND strftime('%m', datetime(dateTimestamp/1000, 'unixepoch')) = :month ORDER BY dateTimestamp DESC")
    LiveData<List<TransactionEntity>> getTransactionsByUserAndMonth(String userId, String year, String month);
}
