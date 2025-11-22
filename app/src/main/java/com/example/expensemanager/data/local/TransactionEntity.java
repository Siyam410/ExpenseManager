package com.example.expensemanager.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class TransactionEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String userId;      // User ID from Firebase Auth
    public double amount;
    public String type;       // "expense" or "income"
    public String category;
    public String wallet;
    public long dateTimestamp;
    public String note;
}

