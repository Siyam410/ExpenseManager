package com.example.expensemanager.ui.main;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.expensemanager.MainActivity;
import com.example.expensemanager.data.local.TransactionEntity;
import com.example.expensemanager.databinding.FragmentDashboardBinding;
import com.example.expensemanager.ui.auth.LoginActivity;
import com.example.expensemanager.utils.BackupManager;
import com.example.expensemanager.utils.BudgetPreferences;
//import com.example.expensemanager.utils.CloudBackupManager;  // HIDDEN - Work in Progress
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private TransactionViewModel viewModel;
    private Calendar selectedMonth;
    private List<TransactionEntity> allTransactions = new ArrayList<>();
    private BackupManager backupManager;

    // Activity result launchers for file pickers
    private ActivityResultLauncher<String> exportLauncher;
    private ActivityResultLauncher<String[]> importLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        backupManager = new BackupManager();

        // Register export file picker
        exportLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/json"),
            uri -> {
                if (uri != null) {
                    performExport(uri);
                }
            }
        );

        // Register import file picker
        importLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    performImport(uri);
                }
            }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        // Initialize with current month
        selectedMonth = Calendar.getInstance();

        observeTransactions();
        setupMonthNavigation();

        binding.fabAddTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddTransactionActivity.class);
            startActivity(intent);
        });

        binding.btnViewMonthlyTransactions.setOnClickListener(v -> openMonthlyTransactions());

        binding.btnExportData.setOnClickListener(v -> startExport());

        binding.btnImportData.setOnClickListener(v -> startImport());

        binding.btnLogout.setOnClickListener(v -> showLogoutConfirmation());

        binding.btnSetBudget.setOnClickListener(v -> showSetBudgetDialog());

        // Initialize budget display
        updateBudgetDisplay();

        // Cloud backup functionality (HIDDEN - Work in Progress)
        // TODO: Uncomment when ready to enable cloud backup
        /*
        binding.btnRestoreFromCloud.setOnClickListener(v -> restoreFromCloud());

        binding.switchAutoBackup.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setAutoBackupEnabled(isChecked);
            updateAutoBackupStatus(isChecked);
            Toast.makeText(requireContext(),
                isChecked ? "Auto-backup enabled" : "Auto-backup disabled",
                Toast.LENGTH_SHORT).show();
        });

        // Initialize auto-backup switch state
        binding.switchAutoBackup.setChecked(viewModel.isAutoBackupEnabled());
        updateAutoBackupStatus(viewModel.isAutoBackupEnabled());

        // Check for existing cloud backup
        checkCloudBackupStatus();
        */

        return binding.getRoot();
    }

    private void observeTransactions() {
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), entities -> {
            if (entities != null) {
                allTransactions = entities;
                updateDashboard(entities);
                updateMonthlyView();
            }
        });
    }

    private void setupMonthNavigation() {
        binding.btnPreviousMonth.setOnClickListener(v -> {
            selectedMonth.add(Calendar.MONTH, -1);
            updateMonthlyView();
        });

        binding.btnNextMonth.setOnClickListener(v -> {
            selectedMonth.add(Calendar.MONTH, 1);
            updateMonthlyView();
        });
    }

    private void updateDashboard(List<TransactionEntity> entities) {
        double totalIncome = 0;
        double totalExpense = 0;
        int incomeCount = 0;
        int expenseCount = 0;

        // Calculate current month expense for budget comparison
        Calendar currentMonth = Calendar.getInstance();
        int currentYear = currentMonth.get(Calendar.YEAR);
        int currentMonthValue = currentMonth.get(Calendar.MONTH);
        double currentMonthExpense = 0;

        for (TransactionEntity entity : entities) {
            android.util.Log.d("DashboardFragment", "Transaction: id=" + entity.id + ", type=" + entity.type + ", amount=" + entity.amount);

            if ("income".equals(entity.type)) {
                totalIncome += entity.amount;
                incomeCount++;
            } else if ("expense".equals(entity.type)) {
                totalExpense += entity.amount;
                expenseCount++;

                // Check if expense is from current month
                Calendar expenseCal = Calendar.getInstance();
                expenseCal.setTimeInMillis(entity.dateTimestamp);
                if (expenseCal.get(Calendar.YEAR) == currentYear &&
                    expenseCal.get(Calendar.MONTH) == currentMonthValue) {
                    currentMonthExpense += entity.amount;
                }
            }
        }

        double balance = totalIncome - totalExpense;

        android.util.Log.d("DashboardFragment", "Income Count: " + incomeCount + ", Expense Count: " + expenseCount);
        android.util.Log.d("DashboardFragment", "Total Income: " + totalIncome + ", Total Expense: " + totalExpense);

        String summary = String.format("Total Income: ৳%.2f\nTotal Expense: ৳%.2f\nBalance: ৳%.2f\n\nTotal Transactions: %d",
                totalIncome, totalExpense, balance, entities.size());
        binding.tvSummary.setText(summary);

        // Update budget display with current month expense
        updateBudgetDisplay(currentMonthExpense);
    }

    private void updateMonthlyView() {
        // Update month label
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        binding.tvCurrentMonth.setText(monthFormat.format(selectedMonth.getTime()));

        // Filter transactions for selected month
        List<TransactionEntity> monthTransactions = filterTransactionsByMonth(allTransactions);

        // Calculate expenses by category
        Map<String, Double> categoryExpenses = new HashMap<>();
        double totalMonthExpense = 0;

        for (TransactionEntity transaction : monthTransactions) {
            if ("expense".equals(transaction.type)) {
                String category = transaction.category != null ? transaction.category : "Others";
                categoryExpenses.put(category, categoryExpenses.getOrDefault(category, 0.0) + transaction.amount);
                totalMonthExpense += transaction.amount;
            }
        }

        // Update expense summary
        binding.tvMonthExpenseSummary.setText(String.format("Total Expense: ৳%.2f", totalMonthExpense));

        // Update pie chart
        if (categoryExpenses.isEmpty()) {
            binding.pieChart.setVisibility(View.GONE);
            binding.tvNoData.setVisibility(View.VISIBLE);
        } else {
            binding.pieChart.setVisibility(View.VISIBLE);
            binding.tvNoData.setVisibility(View.GONE);
            setupPieChart(categoryExpenses);
        }
    }

    private List<TransactionEntity> filterTransactionsByMonth(List<TransactionEntity> transactions) {
        List<TransactionEntity> filtered = new ArrayList<>();

        Calendar transactionCal = Calendar.getInstance();
        int selectedYear = selectedMonth.get(Calendar.YEAR);
        int selectedMonthValue = selectedMonth.get(Calendar.MONTH);

        for (TransactionEntity transaction : transactions) {
            transactionCal.setTimeInMillis(transaction.dateTimestamp);

            if (transactionCal.get(Calendar.YEAR) == selectedYear &&
                transactionCal.get(Calendar.MONTH) == selectedMonthValue) {
                filtered.add(transaction);
            }
        }

        return filtered;
    }

    private void setupPieChart(Map<String, Double> categoryExpenses) {
        PieChart pieChart = binding.pieChart;

        // Prepare data entries
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        // Create dataset
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(getChartColors());
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(2f);

        // Create pie data
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));

        // Configure pie chart
        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("Expenses\nby Category");
        pieChart.setCenterTextSize(14f);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

        // Legend
        pieChart.getLegend().setEnabled(true);
        pieChart.getLegend().setTextSize(11f);
        pieChart.getLegend().setWordWrapEnabled(true);

        // Animate
        pieChart.animateY(1000);

        // Add click listener to open transactions for this month
        pieChart.setOnChartValueSelectedListener(new com.github.mikephil.charting.listener.OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(com.github.mikephil.charting.data.Entry e, com.github.mikephil.charting.highlight.Highlight h) {
                openMonthlyTransactions();
            }

            @Override
            public void onNothingSelected() {
                // Optional: could also open monthly transactions on general chart click
            }
        });

        // Refresh
        pieChart.invalidate();
    }

    private void openMonthlyTransactions() {
        // Navigate to Transactions tab
        // Users can use the month dropdown filter to select the desired month
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            // Switch to transactions tab
            mainActivity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(com.example.expensemanager.R.id.fragment_container, new TransactionsFragment())
                    .commit();

            // Update bottom navigation
            if (mainActivity.findViewById(com.example.expensemanager.R.id.nav_transactions) != null) {
                Toast.makeText(requireContext(), "Use the month filter to view specific months", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private ArrayList<Integer> getChartColors() {
        ArrayList<Integer> colors = new ArrayList<>();

        // Category-specific colors
        colors.add(Color.rgb(76, 175, 80));   // Green - Food
        colors.add(Color.rgb(255, 152, 0));   // Orange - Transport
        colors.add(Color.rgb(156, 39, 176));  // Purple - Shopping
        colors.add(Color.rgb(33, 150, 243));  // Blue - Bills
        colors.add(Color.rgb(0, 150, 136));   // Teal - Health
        colors.add(Color.rgb(255, 235, 59));  // Yellow - Education
        colors.add(Color.rgb(244, 67, 54));   // Red - Entertainment
        colors.add(Color.rgb(158, 158, 158)); // Gray - Others

        // Add more Material Design colors if needed
        for (int color : ColorTemplate.MATERIAL_COLORS) {
            colors.add(color);
        }

        return colors;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to this fragment
        if (viewModel != null) {
            viewModel.getAllTransactions().observe(getViewLifecycleOwner(), entities -> {
                if (entities != null) {
                    allTransactions = entities;
                    updateDashboard(entities);
                    updateMonthlyView();
                }
            });
        }
    }

    /**
     * Start the export process by launching file picker
     */
    private void startExport() {
        String filename = BackupManager.generateBackupFilename();
        exportLauncher.launch(filename);
    }

    /**
     * Perform the actual export to selected file
     */
    private void performExport(Uri uri) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
            ? FirebaseAuth.getInstance().getCurrentUser().getUid()
            : null;

        if (userId == null) {
            Toast.makeText(requireContext(), "Error: User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress dialog
        AlertDialog progressDialog = new AlertDialog.Builder(requireContext())
            .setTitle("Exporting Data")
            .setMessage("Please wait...")
            .setCancelable(false)
            .create();
        progressDialog.show();

        // Fetch all transactions for the user
        viewModel.getRepository().getAllTransactionsSync(userId, transactions -> {
            requireActivity().runOnUiThread(() -> {
                if (transactions == null || transactions.isEmpty()) {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "No transactions to export", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Convert to JSON
                String jsonData = backupManager.exportToJson(transactions);

                if (jsonData != null) {
                    // Write to file
                    boolean success = backupManager.writeJsonToFile(requireContext(), uri, jsonData);

                    progressDialog.dismiss();

                    if (success) {
                        new AlertDialog.Builder(requireContext())
                            .setTitle("Export Successful")
                            .setMessage("Successfully exported " + transactions.size() + " transactions.\n\n" +
                                      "Keep this backup file safe for future restoration.")
                            .setPositiveButton("OK", null)
                            .show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to write backup file", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Failed to create backup", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    /**
     * Start the import process by launching file picker
     */
    private void startImport() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Import Data")
            .setMessage("This will import transactions from a backup file.\n\n" +
                       "Note: Imported transactions will be added to your existing data (no duplicates will be created if you import the same file multiple times).")
            .setPositiveButton("Continue", (dialog, which) -> {
                importLauncher.launch(new String[]{"application/json"});
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Perform the actual import from selected file
     */
    private void performImport(Uri uri) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
            ? FirebaseAuth.getInstance().getCurrentUser().getUid()
            : null;

        if (userId == null) {
            Toast.makeText(requireContext(), "Error: User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress dialog
        AlertDialog progressDialog = new AlertDialog.Builder(requireContext())
            .setTitle("Importing Data")
            .setMessage("Please wait...")
            .setCancelable(false)
            .create();
        progressDialog.show();

        new Thread(() -> {
            // Read and parse JSON file
            List<TransactionEntity> transactions = backupManager.importFromJson(requireContext(), uri);

            requireActivity().runOnUiThread(() -> {
                if (transactions == null || transactions.isEmpty()) {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "No valid transactions found in backup file", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update userId for all imported transactions to current user
                for (TransactionEntity transaction : transactions) {
                    transaction.userId = userId;
                }

                // Insert all transactions
                viewModel.getRepository().insertAll(transactions, (success, count) -> {
                    requireActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();

                        if (success) {
                            new AlertDialog.Builder(requireContext())
                                .setTitle("Import Successful")
                                .setMessage("Successfully imported " + count + " transactions.\n\n" +
                                          "Your data has been restored.")
                                .setPositiveButton("OK", null)
                                .show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to import transactions", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }).start();
    }

    /**
     * Show confirmation dialog before logout
     */
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes", (dialog, which) -> performLogout())
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Perform logout and redirect to login screen
     */
    private void performLogout() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();

        // Clear any local data if needed
        if (viewModel != null) {
            viewModel.refreshUser();
        }

        // Redirect to LoginActivity
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Show confirmation
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Finish the activity
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    /**
     * Show dialog to set monthly budget
     */
    private void showSetBudgetDialog() {
        android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter monthly budget (৳)");

        // Pre-fill with existing budget if set
        double existingBudget = BudgetPreferences.getBudget(requireContext());
        if (existingBudget > 0) {
            input.setText(String.valueOf((int) existingBudget));
        }

        android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(50, 0, 50, 0);
        input.setLayoutParams(lp);

        new AlertDialog.Builder(requireContext())
            .setTitle("Set Monthly Budget")
            .setMessage("Set your monthly expense budget to track your spending")
            .setView(input)
            .setPositiveButton("Set", (dialog, which) -> {
                String budgetStr = input.getText().toString().trim();
                if (!budgetStr.isEmpty()) {
                    try {
                        double budget = Double.parseDouble(budgetStr);
                        if (budget > 0) {
                            BudgetPreferences.saveBudget(requireContext(), budget);
                            updateBudgetDisplay();
                            Toast.makeText(requireContext(), "Budget set to ৳" + String.format(Locale.getDefault(), "%.0f", budget), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Invalid budget amount", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Budget not set", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Clear", (dialog, which) -> {
                BudgetPreferences.clearBudget(requireContext());
                updateBudgetDisplay();
                Toast.makeText(requireContext(), "Budget cleared", Toast.LENGTH_SHORT).show();
            })
            .show();
    }

    /**
     * Update budget display (overload without parameter for initial call)
     */
    private void updateBudgetDisplay() {
        // Calculate current month expense
        Calendar currentMonth = Calendar.getInstance();
        int currentYear = currentMonth.get(Calendar.YEAR);
        int currentMonthValue = currentMonth.get(Calendar.MONTH);
        double currentMonthExpense = 0;

        for (TransactionEntity entity : allTransactions) {
            if ("expense".equals(entity.type)) {
                Calendar expenseCal = Calendar.getInstance();
                expenseCal.setTimeInMillis(entity.dateTimestamp);
                if (expenseCal.get(Calendar.YEAR) == currentYear &&
                    expenseCal.get(Calendar.MONTH) == currentMonthValue) {
                    currentMonthExpense += entity.amount;
                }
            }
        }

        updateBudgetDisplay(currentMonthExpense);
    }

    /**
     * Update budget display with given current month expense
     */
    private void updateBudgetDisplay(double currentMonthExpense) {
        double budget = BudgetPreferences.getBudget(requireContext());

        if (budget <= 0) {
            // No budget set - hide the card
            binding.cardBudget.setVisibility(View.GONE);
            return;
        }

        // Show the card
        binding.cardBudget.setVisibility(View.VISIBLE);

        // Calculate percentage
        double percentage = (currentMonthExpense / budget) * 100;
        int progressValue = (int) Math.min(percentage, 100);

        // Update UI
        binding.tvBudgetPercentage.setText(String.format(Locale.getDefault(), "%.0f%%", percentage));
        binding.tvBudgetDetails.setText(String.format(Locale.getDefault(),
                "Spent: ৳%.0f of ৳%.0f", currentMonthExpense, budget));
        binding.progressBudget.setProgress(progressValue);

        double remaining = budget - currentMonthExpense;
        if (remaining >= 0) {
            binding.tvBudgetRemaining.setText(String.format(Locale.getDefault(),
                    "Remaining: ৳%.0f", remaining));
            binding.tvBudgetRemaining.setTextColor(Color.parseColor("#388E3C")); // Green

            // Change progress bar color based on percentage
            if (percentage < 50) {
                // Green - safe
                binding.progressBudget.getProgressDrawable().setColorFilter(
                        Color.parseColor("#4CAF50"), android.graphics.PorterDuff.Mode.SRC_IN);
                binding.cardBudget.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
                binding.tvBudgetPercentage.setTextColor(Color.parseColor("#2E7D32"));
            } else if (percentage < 80) {
                // Yellow - warning
                binding.progressBudget.getProgressDrawable().setColorFilter(
                        Color.parseColor("#FFC107"), android.graphics.PorterDuff.Mode.SRC_IN);
                binding.cardBudget.setCardBackgroundColor(Color.parseColor("#FFF8E1"));
                binding.tvBudgetPercentage.setTextColor(Color.parseColor("#F57C00"));
            } else {
                // Red - danger
                binding.progressBudget.getProgressDrawable().setColorFilter(
                        Color.parseColor("#F44336"), android.graphics.PorterDuff.Mode.SRC_IN);
                binding.cardBudget.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
                binding.tvBudgetPercentage.setTextColor(Color.parseColor("#C62828"));
            }
        } else {
            // Over budget
            binding.tvBudgetRemaining.setText(String.format(Locale.getDefault(),
                    "Over budget by ৳%.0f!", Math.abs(remaining)));
            binding.tvBudgetRemaining.setTextColor(Color.parseColor("#D32F2F")); // Red
            binding.progressBudget.getProgressDrawable().setColorFilter(
                    Color.parseColor("#F44336"), android.graphics.PorterDuff.Mode.SRC_IN);
            binding.cardBudget.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
            binding.tvBudgetPercentage.setTextColor(Color.parseColor("#C62828"));
        }
    }

    /* HIDDEN - Cloud Backup Methods (Work in Progress)
    // TODO: Uncomment when ready to enable cloud backup

    /**
     * Update auto-backup status text
     */
    /*
    private void updateAutoBackupStatus(boolean enabled) {
        if (enabled) {
            binding.tvAutoBackupStatus.setText("✅ Auto-backup enabled - saves after each transaction");
            binding.tvAutoBackupStatus.setTextColor(Color.parseColor("#1976D2"));
        } else {
            binding.tvAutoBackupStatus.setText("⚠️ Auto-backup disabled");
            binding.tvAutoBackupStatus.setTextColor(Color.parseColor("#F57C00"));
        }
    }

    /**
     * Check if cloud backup exists and update UI
     */
    /*
    private void checkCloudBackupStatus() {
        CloudBackupManager cloudBackupManager = viewModel.getCloudBackupManager();

        cloudBackupManager.checkBackupExists((exists, lastBackupDate) -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (exists && lastBackupDate != null) {
                        binding.tvLastCloudBackup.setText("Last backup: " + lastBackupDate);
                        binding.tvLastCloudBackup.setTextColor(Color.parseColor("#388E3C"));
                        binding.btnRestoreFromCloud.setEnabled(true);
                    } else {
                        binding.tvLastCloudBackup.setText("No cloud backup found");
                        binding.tvLastCloudBackup.setTextColor(Color.parseColor("#D32F2F"));
                        binding.btnRestoreFromCloud.setEnabled(false);
                    }
                });
            }
        });
    }

    /**
     * Restore transactions from cloud backup with one click
     */
    /*
    private void restoreFromCloud() {
        new AlertDialog.Builder(requireContext())
            .setTitle("☁️ Restore from Cloud")
            .setMessage("This will restore your transactions from the cloud backup.\n\n" +
                       "Your existing data will be preserved, and backed up transactions will be added.")
            .setPositiveButton("Restore", (dialog, which) -> performCloudRestore())
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Perform the cloud restore operation
     */
    /*
    private void performCloudRestore() {
        AlertDialog progressDialog = new AlertDialog.Builder(requireContext())
            .setTitle("Restoring from Cloud")
            .setMessage("Please wait...")
            .setCancelable(false)
            .create();
        progressDialog.show();

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
            ? FirebaseAuth.getInstance().getCurrentUser().getUid()
            : null;

        if (userId == null) {
            progressDialog.dismiss();
            Toast.makeText(requireContext(), "Error: User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        CloudBackupManager cloudBackupManager = viewModel.getCloudBackupManager();

        cloudBackupManager.restoreFromCloud(new CloudBackupManager.OnCloudRestoreListener() {
            @Override
            public void onRestoreSuccess(List<TransactionEntity> transactions, String backupDate) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Update userId for all imported transactions
                        for (TransactionEntity transaction : transactions) {
                            transaction.userId = userId;
                        }

                        // Insert all transactions
                        viewModel.getRepository().insertAll(transactions, (success, count) -> {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    progressDialog.dismiss();

                                    if (success) {
                                        new AlertDialog.Builder(requireContext())
                                            .setTitle("✅ Restore Successful")
                                            .setMessage("Successfully restored " + count + " transactions from cloud backup.\n\n" +
                                                      "Backup date: " + backupDate + "\n\n" +
                                                      "Your data has been restored!")
                                            .setPositiveButton("OK", (d, w) -> {
                                                // Refresh the dashboard
                                                checkCloudBackupStatus();
                                            })
                                            .show();
                                    } else {
                                        Toast.makeText(requireContext(), "Failed to restore transactions", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    });
                }
            }

            @Override
            public void onRestoreFailed(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();

                        new AlertDialog.Builder(requireContext())
                            .setTitle("❌ Restore Failed")
                            .setMessage("Could not restore from cloud:\n\n" + error)
                            .setPositiveButton("OK", null)
                            .show();
                    });
                }
            }
        });
    }
    */
}
