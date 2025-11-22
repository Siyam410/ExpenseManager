package com.example.expensemanager.ui.main;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    public interface OnTransactionLongClickListener {
        void onTransactionLongClick(long transactionId, int position);
    }

    public static class TransactionItem {
        public long id;
        public String category;
        public String title;
        public double amount;
        public String walletName;
        public String dateText;
        public boolean isExpense;

        public TransactionItem(long id, String category, String title, double amount, String walletName, String dateText, boolean isExpense) {
            this.id = id;
            this.category = category;
            this.title = title;
            this.amount = amount;
            this.walletName = walletName;
            this.dateText = dateText;
            this.isExpense = isExpense;
        }
    }

    private final Context context;
    private List<TransactionItem> items;
    private final HashMap<String, Integer> categoryColorMap = new HashMap<>();
    private OnTransactionLongClickListener longClickListener;

    public TransactionAdapter(Context context, List<TransactionItem> items) {
        this.context = context;
        this.items = items;
        initCategoryColors();
    }

    public void setOnTransactionLongClickListener(OnTransactionLongClickListener listener) {
        this.longClickListener = listener;
    }

    private void initCategoryColors() {
        categoryColorMap.put("Food", 0xFF4CAF50);       // Green
        categoryColorMap.put("Transport", 0xFFFF9800);  // Orange
        categoryColorMap.put("Shopping", 0xFF9C27B0);   // Purple
        categoryColorMap.put("Bills", 0xFF2196F3);      // Blue
        categoryColorMap.put("Health", 0xFF009688);     // Teal
        categoryColorMap.put("Education", 0xFFFFEB3B);  // Yellow
        categoryColorMap.put("Others", 0xFF9E9E9E);     // Grey
    }

    public void updateItems(List<TransactionItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionItem item = items.get(position);

        holder.tvTitle.setText(item.title);
        holder.tvWallet.setText(item.walletName);
        holder.tvDate.setText(item.dateText);

        // Bangladeshi Taka formatting
        NumberFormat format = NumberFormat.getNumberInstance(new Locale("bn", "BD"));
        String amountFormatted = "\u09F3" + format.format(item.amount); // 	F3 = Taka sign
        holder.tvAmount.setText(amountFormatted);

        // Expense vs income color
        String typeText = item.isExpense ? "EXPENSE" : "INCOME";
        android.util.Log.d("TransactionAdapter", "Displaying transaction ID=" + item.id + ", Type=" + typeText + ", Amount=" + item.amount);

        int amountColor = ContextCompat.getColor(context,
                item.isExpense ? R.color.expenseColor : R.color.incomeColor);
        holder.tvAmount.setTextColor(amountColor);

        // Set category icon from PNG files
        int categoryIcon = getCategoryIcon(item.category);
        holder.ivCategoryIcon.setImageResource(categoryIcon);
        holder.ivCategoryIcon.setColorFilter(null); // Remove color filter to show original icon colors

        // Long click listener
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onTransactionLongClick(item.id, position);
                return true;
            }
            return false;
        });
    }

    /**
     * Get category icon resource based on category name
     */
    private int getCategoryIcon(String category) {
        if (category == null) {
            return R.drawable.money; // Default icon
        }

        switch (category) {
            case "Food":
                return R.drawable.dish;              // Dish icon for food
            case "Transport":
                return R.drawable.transport;         // Transport icon
            case "Shopping":
                return R.drawable.bag;               // Shopping bag icon
            case "Bills":
                return R.drawable.bill;              // Bill/receipt icon
            case "Health":
                return R.drawable.healthcare;        // Healthcare icon
            case "Education":
                return R.drawable.bachelor;          // Education/graduation icon
            case "Entertainment":
                return R.drawable.dataanalysis;      // Data/entertainment icon
            case "Groceries":
                return R.drawable.bag;               // Reuse bag for groceries
            case "Utilities":
                return R.drawable.automated;         // Automated/utilities icon
            case "Rent":
                return R.drawable.wallet;            // Wallet icon for rent
            case "Others":
            default:
                return R.drawable.money;             // Money icon for others
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryIcon;
        TextView tvTitle;
        TextView tvWallet;
        TextView tvDate;
        TextView tvAmount;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvWallet = itemView.findViewById(R.id.tvWallet);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}
