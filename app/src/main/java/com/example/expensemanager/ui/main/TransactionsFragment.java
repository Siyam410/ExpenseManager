package com.example.expensemanager.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.expensemanager.data.local.TransactionEntity;
import com.example.expensemanager.databinding.FragmentTransactionsBinding;
import com.example.expensemanager.ui.main.adapters.CategorySpinnerAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionsFragment extends Fragment {

    private FragmentTransactionsBinding binding;
    private TransactionAdapter adapter;
    private TransactionViewModel viewModel;
    private Map<Long, TransactionEntity> transactionMap = new HashMap<>();
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    // Filter parameters
    private List<String> monthOptions = new ArrayList<>();
    private List<String> categoryOptions = new ArrayList<>();
    private String selectedMonthYear = "All Months";
    private String selectedCategory = "All Categories";

    private List<TransactionEntity> allTransactions = new ArrayList<>();

    public static TransactionsFragment newInstance() {
        return new TransactionsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTransactionsBinding.inflate(inflater, container, false);

        binding.rvTransactions.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        adapter = new TransactionAdapter(requireContext(), new ArrayList<>());
        binding.rvTransactions.setAdapter(adapter);

        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        // Set long click listener
        adapter.setOnTransactionLongClickListener(this::showEditDeleteDialog);

        // Setup filters
        setupMonthDropdown();
        setupCategoryDropdown();

        // Observe transactions
        observeTransactions();

        // Button listeners
        binding.btnShowAll.setOnClickListener(v -> {
            selectedMonthYear = "All Months";
            selectedCategory = "All Categories";
            binding.spinnerMonth.setSelection(0);
            binding.spinnerCategory.setSelection(0);
            applyFilters();
            Toast.makeText(requireContext(), "Showing all transactions", Toast.LENGTH_SHORT).show();
        });

        binding.btnApplyFilter.setOnClickListener(v -> {
            applyFilters();
            String filterMsg = "Filter: " + selectedMonthYear;
            if (!selectedCategory.equals("All Categories")) {
                filterMsg += " - " + selectedCategory;
            }
            Toast.makeText(requireContext(), filterMsg, Toast.LENGTH_SHORT).show();
        });

        binding.fabAddTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddTransactionActivity.class);
            startActivity(intent);
        });

        return binding.getRoot();
    }

    private void setupMonthDropdown() {
        monthOptions.clear();
        monthOptions.add("All Months");

        // Generate last 12 months
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

        for (int i = 0; i < 12; i++) {
            monthOptions.add(monthFormat.format(cal.getTime()));
            cal.add(Calendar.MONTH, -1);
        }

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                monthOptions
        );
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerMonth.setAdapter(monthAdapter);

        binding.spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMonthYear = monthOptions.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupCategoryDropdown() {
        categoryOptions.clear();
        categoryOptions.add("All Categories");
        categoryOptions.add("Food");
        categoryOptions.add("Transport");
        categoryOptions.add("Shopping");
        categoryOptions.add("Bills");
        categoryOptions.add("Health");
        categoryOptions.add("Education");
        categoryOptions.add("Entertainment");
        categoryOptions.add("Groceries");
        categoryOptions.add("Utilities");
        categoryOptions.add("Rent");
        categoryOptions.add("Others");

        // Use custom adapter with icons
        CategorySpinnerAdapter categoryAdapter = new CategorySpinnerAdapter(
                requireContext(),
                categoryOptions
        );
        binding.spinnerCategory.setAdapter(categoryAdapter);

        binding.spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = categoryOptions.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void observeTransactions() {
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), entities -> {
            if (entities != null) {
                allTransactions = new ArrayList<>(entities);
                applyFilters();
            }
        });
    }

    private void applyFilters() {
        List<TransactionEntity> filteredTransactions = new ArrayList<>();

        for (TransactionEntity entity : allTransactions) {
            boolean matchesMonth = true;
            boolean matchesCategory = true;

            // Month filter
            if (!selectedMonthYear.equals("All Months")) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(entity.dateTimestamp);
                SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
                String transactionMonthYear = monthFormat.format(cal.getTime());
                matchesMonth = transactionMonthYear.equals(selectedMonthYear);
            }

            // Category filter
            if (!selectedCategory.equals("All Categories")) {
                String entityCategory = entity.category != null ? entity.category : "Others";
                matchesCategory = entityCategory.equals(selectedCategory);
            }

            if (matchesMonth && matchesCategory) {
                filteredTransactions.add(entity);
            }
        }

        updateTransactionsList(filteredTransactions);
    }

    private void updateTransactionsList(List<TransactionEntity> entities) {
        transactionMap.clear();
        List<TransactionAdapter.TransactionItem> items = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        if (entities == null) {
            entities = new ArrayList<>();
        }

        for (TransactionEntity e : entities) {
            transactionMap.put(e.id, e);
            String dateText = sdf.format(new Date(e.dateTimestamp));
            boolean isExpense = "expense".equals(e.type);
            String title = (e.note != null && !e.note.isEmpty()) ? e.note : e.category;


            items.add(new TransactionAdapter.TransactionItem(
                    e.id,
                    e.category,
                    title,
                    e.amount,
                    e.wallet,
                    dateText,
                    isExpense
            ));
        }
        adapter.updateItems(items);
    }

    private void showEditDeleteDialog(long transactionId, int position) {
        TransactionEntity transaction = transactionMap.get(transactionId);
        if (transaction == null) {
            Toast.makeText(requireContext(), "Transaction not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] options = {"Edit", "Delete"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Transaction Options")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Edit
                        editTransaction(transactionId);
                    } else {
                        // Delete
                        confirmDelete(transaction);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void editTransaction(long transactionId) {
        Intent intent = new Intent(requireContext(), AddTransactionActivity.class);
        intent.putExtra("EDIT_MODE", true);
        intent.putExtra("TRANSACTION_ID", transactionId);
        startActivity(intent);
    }

    private void confirmDelete(TransactionEntity transaction) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    executor.execute(() -> {
                        viewModel.delete(transaction);
                        requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Transaction deleted", Toast.LENGTH_SHORT).show()
                        );
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
