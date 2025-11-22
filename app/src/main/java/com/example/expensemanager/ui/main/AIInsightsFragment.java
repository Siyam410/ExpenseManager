package com.example.expensemanager.ui.main;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.expensemanager.data.local.TransactionEntity;
import com.example.expensemanager.databinding.FragmentAiInsightsBinding;
import com.example.expensemanager.ui.main.models.CategoryInsight;
import com.example.expensemanager.utils.CategoryColors;
import com.example.expensemanager.utils.MotivationalQuotes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AIInsightsFragment extends Fragment {

    private FragmentAiInsightsBinding binding;
    private TransactionViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAiInsightsBinding.inflate(inflater, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        // Show loading
        binding.progressLoading.setVisibility(View.VISIBLE);

        // Set random motivational quote
        setMotivationalQuote();

        // Observe transactions and calculate insights
        observeTransactions();

        return binding.getRoot();
    }

    private void setMotivationalQuote() {
        String quote = MotivationalQuotes.getRandomQuote();
        binding.tvMotivationalQuote.setText(quote);
    }

    private void observeTransactions() {
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                calculateAndDisplayInsights(transactions);
            }
            binding.progressLoading.setVisibility(View.GONE);
        });
    }

    private void calculateAndDisplayInsights(List<TransactionEntity> allTransactions) {
        // Filter only expense transactions
        List<TransactionEntity> expenses = new ArrayList<>();
        for (TransactionEntity t : allTransactions) {
            if ("expense".equals(t.type)) {
                expenses.add(t);
            }
        }

        if (expenses.isEmpty()) {
            showNoDataState();
            return;
        }

        // Calculate insights
        calculateTop3CategoriesOfYear(expenses);
        calculateMonthlyComparison(expenses);
    }

    private void calculateTop3CategoriesOfYear(List<TransactionEntity> expenses) {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        // Update year label
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        String currentMonth = monthFormat.format(calendar.getTime());
        binding.tvYearLabel.setText("January - " + currentMonth + " " + currentYear);

        // Calculate category totals for current year
        Map<String, Double> categoryTotals = new HashMap<>();

        for (TransactionEntity expense : expenses) {
            Calendar expenseCal = Calendar.getInstance();
            expenseCal.setTimeInMillis(expense.dateTimestamp);

            if (expenseCal.get(Calendar.YEAR) == currentYear) {
                String category = expense.category != null ? expense.category : "Others";
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + expense.amount);
            }
        }

        if (categoryTotals.isEmpty()) {
            binding.tvNoYearData.setVisibility(View.VISIBLE);
            binding.layoutCategory1.setVisibility(View.GONE);
            binding.layoutCategory2.setVisibility(View.GONE);
            binding.layoutCategory3.setVisibility(View.GONE);
            return;
        }

        // Convert to list and sort
        List<CategoryInsight> insights = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            CategoryInsight insight = new CategoryInsight(
                    entry.getKey(),
                    entry.getValue(),
                    CategoryColors.getColorForCategory(entry.getKey())
            );
            insights.add(insight);
        }

        // Sort by amount descending
        Collections.sort(insights, (a, b) -> Double.compare(b.getTotalAmount(), a.getTotalAmount()));

        // Display top 3
        displayTop3Categories(insights);
    }

    private void displayTop3Categories(List<CategoryInsight> insights) {
        binding.tvNoYearData.setVisibility(View.GONE);

        double maxAmount = insights.get(0).getTotalAmount();

        // Category 1
        if (insights.size() >= 1) {
            CategoryInsight cat1 = insights.get(0);
            binding.layoutCategory1.setVisibility(View.VISIBLE);
            binding.tvCategory1Name.setText(cat1.getCategoryName());
            binding.tvCategory1Amount.setText(String.format(Locale.getDefault(), "৳%.0f", cat1.getTotalAmount()));
            binding.progressCategory1.setProgress(100);
            binding.progressCategory1.getProgressDrawable().setColorFilter(cat1.getCategoryColor(), PorterDuff.Mode.SRC_IN);
        } else {
            binding.layoutCategory1.setVisibility(View.GONE);
        }

        // Category 2
        if (insights.size() >= 2) {
            CategoryInsight cat2 = insights.get(1);
            binding.layoutCategory2.setVisibility(View.VISIBLE);
            binding.tvCategory2Name.setText(cat2.getCategoryName());
            binding.tvCategory2Amount.setText(String.format(Locale.getDefault(), "৳%.0f", cat2.getTotalAmount()));
            int progress2 = (int) ((cat2.getTotalAmount() / maxAmount) * 100);
            binding.progressCategory2.setProgress(progress2);
            binding.progressCategory2.getProgressDrawable().setColorFilter(cat2.getCategoryColor(), PorterDuff.Mode.SRC_IN);
        } else {
            binding.layoutCategory2.setVisibility(View.GONE);
        }

        // Category 3
        if (insights.size() >= 3) {
            CategoryInsight cat3 = insights.get(2);
            binding.layoutCategory3.setVisibility(View.VISIBLE);
            binding.tvCategory3Name.setText(cat3.getCategoryName());
            binding.tvCategory3Amount.setText(String.format(Locale.getDefault(), "৳%.0f", cat3.getTotalAmount()));
            int progress3 = (int) ((cat3.getTotalAmount() / maxAmount) * 100);
            binding.progressCategory3.setProgress(progress3);
            binding.progressCategory3.getProgressDrawable().setColorFilter(cat3.getCategoryColor(), PorterDuff.Mode.SRC_IN);
        } else {
            binding.layoutCategory3.setVisibility(View.GONE);
        }
    }

    private void calculateMonthlyComparison(List<TransactionEntity> expenses) {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);

        // Calculate this month's category totals
        Map<String, Double> thisMonthTotals = new HashMap<>();
        for (TransactionEntity expense : expenses) {
            Calendar expenseCal = Calendar.getInstance();
            expenseCal.setTimeInMillis(expense.dateTimestamp);

            if (expenseCal.get(Calendar.YEAR) == currentYear &&
                expenseCal.get(Calendar.MONTH) == currentMonth) {
                String category = expense.category != null ? expense.category : "Others";
                thisMonthTotals.put(category, thisMonthTotals.getOrDefault(category, 0.0) + expense.amount);
            }
        }

        // Calculate last month's category totals
        calendar.add(Calendar.MONTH, -1);
        int lastMonthYear = calendar.get(Calendar.YEAR);
        int lastMonth = calendar.get(Calendar.MONTH);

        Map<String, Double> lastMonthTotals = new HashMap<>();
        for (TransactionEntity expense : expenses) {
            Calendar expenseCal = Calendar.getInstance();
            expenseCal.setTimeInMillis(expense.dateTimestamp);

            if (expenseCal.get(Calendar.YEAR) == lastMonthYear &&
                expenseCal.get(Calendar.MONTH) == lastMonth) {
                String category = expense.category != null ? expense.category : "Others";
                lastMonthTotals.put(category, lastMonthTotals.getOrDefault(category, 0.0) + expense.amount);
            }
        }

        // Find highest increase and decrease
        findAndDisplayChanges(thisMonthTotals, lastMonthTotals);
    }

    private void findAndDisplayChanges(Map<String, Double> thisMonth, Map<String, Double> lastMonth) {
        String maxIncreaseCategory = null;
        double maxIncreasePercent = 0;
        double maxIncreaseThisAmount = 0;
        double maxIncreaseLastAmount = 0;

        String maxDecreaseCategory = null;
        double maxDecreasePercent = 0;
        double maxDecreaseThisAmount = 0;
        double maxDecreaseLastAmount = 0;

        // Check all categories
        for (String category : thisMonth.keySet()) {
            double thisAmount = thisMonth.get(category);
            double lastAmount = lastMonth.getOrDefault(category, 0.0);

            if (lastAmount > 0) {
                double change = thisAmount - lastAmount;
                double percentChange = (change / lastAmount) * 100;

                if (percentChange > maxIncreasePercent) {
                    maxIncreasePercent = percentChange;
                    maxIncreaseCategory = category;
                    maxIncreaseThisAmount = thisAmount;
                    maxIncreaseLastAmount = lastAmount;
                }

                if (percentChange < maxDecreasePercent) {
                    maxDecreasePercent = percentChange;
                    maxDecreaseCategory = category;
                    maxDecreaseThisAmount = thisAmount;
                    maxDecreaseLastAmount = lastAmount;
                }
            }
        }

        // Display results
        if (maxIncreaseCategory != null && maxIncreasePercent > 5) {
            binding.cardIncrease.setVisibility(View.VISIBLE);
            binding.tvIncreaseCategory.setText(maxIncreaseCategory);
            binding.tvIncreasePercentage.setText(String.format(Locale.getDefault(),
                    "%.0f%% more than last month", maxIncreasePercent));
            binding.tvIncreaseDetails.setText(String.format(Locale.getDefault(),
                    "Last month: ৳%.0f → This month: ৳%.0f",
                    maxIncreaseLastAmount, maxIncreaseThisAmount));
        } else {
            binding.cardIncrease.setVisibility(View.GONE);
        }

        if (maxDecreaseCategory != null && maxDecreasePercent < -5) {
            binding.cardDecrease.setVisibility(View.VISIBLE);
            binding.tvDecreaseCategory.setText(maxDecreaseCategory);
            binding.tvDecreasePercentage.setText(String.format(Locale.getDefault(),
                    "You spent %.0f%% less on %s this month!",
                    Math.abs(maxDecreasePercent), maxDecreaseCategory));
            binding.tvDecreaseDetails.setText(String.format(Locale.getDefault(),
                    "Last month: ৳%.0f → This month: ৳%.0f",
                    maxDecreaseLastAmount, maxDecreaseThisAmount));
        } else {
            binding.cardDecrease.setVisibility(View.GONE);
        }

        // Show "no change" message if both are hidden
        if (binding.cardIncrease.getVisibility() == View.GONE &&
            binding.cardDecrease.getVisibility() == View.GONE) {
            binding.cardNoChange.setVisibility(View.VISIBLE);
        } else {
            binding.cardNoChange.setVisibility(View.GONE);
        }
    }

    private void showNoDataState() {
        binding.tvNoYearData.setVisibility(View.VISIBLE);
        binding.layoutCategory1.setVisibility(View.GONE);
        binding.layoutCategory2.setVisibility(View.GONE);
        binding.layoutCategory3.setVisibility(View.GONE);
        binding.cardIncrease.setVisibility(View.GONE);
        binding.cardDecrease.setVisibility(View.GONE);
        binding.cardNoChange.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

