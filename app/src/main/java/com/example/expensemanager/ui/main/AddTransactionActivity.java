package com.example.expensemanager.ui.main;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.expensemanager.R;
import com.example.expensemanager.data.local.TransactionEntity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    private EditText etAmount;
    private RadioGroup rgType;
    private RadioButton rbExpense, rbIncome;
    private Spinner spCategory, spWallet;
    private Button btnPickDate, btnSave, btnClear, btnAttachReceipt;
    private EditText etNote;

    private long selectedDateMillis;
    private final Calendar calendar = Calendar.getInstance();

    private TransactionViewModel viewModel;
    private boolean isEditMode = false;
    private long transactionId = -1;
    private TransactionEntity existingEntity = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        initViews();
        setupSpinners();
        setupRadioGroupListener(); // New logic for Income UI toggle
        setupDatePicker();

        // Check if in edit mode
        checkEditMode();

        setupButtons();
    }

    private void initViews() {
        etAmount = findViewById(R.id.etAmount);
        rgType = findViewById(R.id.rgType);
        rbExpense = findViewById(R.id.rbExpense);
        rbIncome = findViewById(R.id.rbIncome);
        spCategory = findViewById(R.id.spCategory);
        spWallet = findViewById(R.id.spWallet);
        btnPickDate = findViewById(R.id.btnPickDate);
        etNote = findViewById(R.id.etNote);
        btnAttachReceipt = findViewById(R.id.btnAttachReceipt);
        btnSave = findViewById(R.id.btnSave);
        btnClear = findViewById(R.id.btnClear);
    }

    private void setupSpinners() {
        // Categories
        String[] categories = new String[]{
                "Food", "Transport", "Shopping", "Bills",
                "Health", "Education", "Entertainment", "Others"
        };
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(catAdapter);

        // Wallets
        String[] wallets = new String[]{"Cash", "bKash", "Nagad", "Bank"};
        ArrayAdapter<String> walletAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                wallets
        );
        walletAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spWallet.setAdapter(walletAdapter);
    }

    // NEW: Toggle Spinner visibility based on selection
    private void setupRadioGroupListener() {
        rgType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbIncome) {
                // Hide category spinner for Income
                spCategory.setVisibility(View.GONE);
            } else {
                // Show category spinner for Expense
                spCategory.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupDatePicker() {
        // Default: today
        selectedDateMillis = calendar.getTimeInMillis();
        updateDateButtonLabel();

        btnPickDate.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, y, m, d) -> {
                        calendar.set(Calendar.YEAR, y);
                        calendar.set(Calendar.MONTH, m);
                        calendar.set(Calendar.DAY_OF_MONTH, d);
                        selectedDateMillis = calendar.getTimeInMillis();
                        updateDateButtonLabel();
                    },
                    year, month, day
            );
            dialog.show();
        });
    }

    private void updateDateButtonLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String text = "Date: " + sdf.format(calendar.getTime());
        btnPickDate.setText(text);
    }

    private void setupButtons() {
        btnSave.setOnClickListener(v -> {
            if (!validate()) return;

            double amount = Double.parseDouble(etAmount.getText().toString().trim());

            // Logic to determine Type and Category
            String type;
            String category;

            if (rbIncome.isChecked()) {
                type = "income";
                category = "Income"; // Force category name to "Income"
            } else {
                type = "expense";
                category = (String) spCategory.getSelectedItem(); // Use selected spinner item
            }

            String wallet = (String) spWallet.getSelectedItem();
            String note = etNote.getText().toString().trim();
            long dateTimestamp = selectedDateMillis;

            if (isEditMode && existingEntity != null) {
                // Update existing transaction
                existingEntity.amount = amount;
                existingEntity.type = type;
                existingEntity.category = category;
                existingEntity.wallet = wallet;
                existingEntity.dateTimestamp = dateTimestamp;
                existingEntity.note = note;

                viewModel.update(existingEntity);
                Toast.makeText(this, "Updated: " + category, Toast.LENGTH_SHORT).show();
            } else {
                // Insert new transaction
                TransactionEntity entity = new TransactionEntity();
                entity.amount = amount;
                entity.type = type;
                entity.category = category;
                entity.wallet = wallet;
                entity.dateTimestamp = dateTimestamp;
                entity.note = note;

                viewModel.insert(entity);
                Toast.makeText(this, "Saved: " + category, Toast.LENGTH_SHORT).show();
            }

            finish();
        });

        btnClear.setOnClickListener(v -> {
            etAmount.setText("");
            rgType.check(R.id.rbExpense); // This will trigger the listener to show spinner
            spCategory.setSelection(0);
            spWallet.setSelection(0);
            calendar.setTimeInMillis(System.currentTimeMillis());
            selectedDateMillis = calendar.getTimeInMillis();
            updateDateButtonLabel();
            etNote.setText("");
        });

        btnAttachReceipt.setOnClickListener(v -> {
            Toast.makeText(this, "Receipt attach not implemented yet", Toast.LENGTH_SHORT).show();
        });
    }

    private void checkEditMode() {
        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("EDIT_MODE", false);

        if (isEditMode) {
            setTitle("Edit Transaction");
            transactionId = intent.getLongExtra("TRANSACTION_ID", -1);

            new Thread(() -> {
                existingEntity = viewModel.getById(transactionId);

                if (existingEntity != null) {
                    runOnUiThread(() -> {
                        etAmount.setText(String.valueOf(existingEntity.amount));

                        if ("income".equals(existingEntity.type)) {
                            rbIncome.setChecked(true);
                            spCategory.setVisibility(View.GONE); // Ensure spinner is hidden
                        } else {
                            rbExpense.setChecked(true);
                            spCategory.setVisibility(View.VISIBLE); // Ensure spinner is visible

                            // Set category spinner selection
                            ArrayAdapter catAdapter = (ArrayAdapter) spCategory.getAdapter();
                            int catPosition = catAdapter.getPosition(existingEntity.category);
                            if (catPosition >= 0) {
                                spCategory.setSelection(catPosition);
                            }
                        }

                        // Set wallet spinner
                        ArrayAdapter walletAdapter = (ArrayAdapter) spWallet.getAdapter();
                        int walletPosition = walletAdapter.getPosition(existingEntity.wallet);
                        if (walletPosition >= 0) {
                            spWallet.setSelection(walletPosition);
                        }

                        // Set date
                        calendar.setTimeInMillis(existingEntity.dateTimestamp);
                        selectedDateMillis = existingEntity.dateTimestamp;
                        updateDateButtonLabel();

                        // Set note
                        if (existingEntity.note != null) {
                            etNote.setText(existingEntity.note);
                        }

                        btnSave.setText("Update");
                    });
                }
            }).start();
        }
    }

    private boolean validate() {
        String amountStr = etAmount.getText().toString().trim();
        if (TextUtils.isEmpty(amountStr)) {
            etAmount.setError("Amount is required");
            etAmount.requestFocus();
            return false;
        }
        try {
            double value = Double.parseDouble(amountStr);
            if (value == 0d) {
                etAmount.setError("Amount cannot be zero");
                etAmount.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount");
            etAmount.requestFocus();
            return false;
        }

        // Only check Category spinner if it is an EXPENSE
        if (rbExpense.isChecked()) {
            if (spCategory.getSelectedItem() == null) {
                Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (spWallet.getSelectedItem() == null) {
            Toast.makeText(this, "Please select a wallet", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}