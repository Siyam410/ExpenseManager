package com.example.expensemanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.expensemanager.databinding.ActivityMainBinding;
import com.example.expensemanager.ui.main.DashboardFragment;
import com.example.expensemanager.ui.main.AIInsightsFragment;
import com.example.expensemanager.ui.main.TransactionsFragment;
import com.example.expensemanager.utils.BudgetPreferences;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final String PREF_FIRST_TIME = "FirstTimeUser";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Check if first time user and show budget setup
        checkAndShowBudgetSetup();

        // Default fragment
        if (savedInstanceState == null) {
            replaceFragment(new DashboardFragment());
        }

        binding.bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                Fragment selected = null;
                int id = item.getItemId();
                if (id == R.id.nav_dashboard) {
                    selected = new DashboardFragment();
                } else if (id == R.id.nav_transactions) {
                    selected = new TransactionsFragment();
                } else if (id == R.id.nav_ai_insights) {
                    selected = new AIInsightsFragment();
                }
                if (selected != null) {
                    replaceFragment(selected);
                    return true;
                }
                return false;
            }
        });
    }

    private void checkAndShowBudgetSetup() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();
        SharedPreferences prefs = getSharedPreferences(PREF_FIRST_TIME, MODE_PRIVATE);

        // Use a specific key for this user to track if they've seen the welcome dialog
        String userKey = "welcomed_" + userId;
        boolean hasSeenWelcome = prefs.getBoolean(userKey, false);

        // Only show welcome dialog if user hasn't seen it before
        if (!hasSeenWelcome) {
            // Mark that this user has now seen the welcome dialog
            prefs.edit().putBoolean(userKey, true).apply();

            // Show budget setup dialog
            showWelcomeBudgetDialog();
        }
    }

    private void showWelcomeBudgetDialog() {
        EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter monthly budget (৳)");

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(50, 0, 50, 0);
        input.setLayoutParams(lp);

        new AlertDialog.Builder(this)
            .setTitle("🎉 Welcome to Expense Manager!")
            .setMessage("Set your monthly expense budget to start tracking your spending goals.\n\nYou can change this anytime from the Dashboard.")
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("Set Budget", (dialog, which) -> {
                String budgetStr = input.getText().toString().trim();
                if (!budgetStr.isEmpty()) {
                    try {
                        double budget = Double.parseDouble(budgetStr);
                        if (budget > 0) {
                            BudgetPreferences.saveBudget(this, budget);
                            Toast.makeText(this, "Budget set to ৳" + String.format(Locale.getDefault(), "%.0f", budget), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Budget not set. You can set it later from Dashboard.", Toast.LENGTH_LONG).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid amount. You can set budget later from Dashboard.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "You can set budget later from the Dashboard.", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Skip", (dialog, which) -> {
                Toast.makeText(this, "You can set budget anytime from the Budget button.", Toast.LENGTH_SHORT).show();
            })
            .show();
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}