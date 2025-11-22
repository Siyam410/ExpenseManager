package com.example.expensemanager.ui.main.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.expensemanager.R;

import java.util.List;

/**
 * Custom adapter for category spinner with icons
 */
public class CategorySpinnerAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final List<String> categories;

    public CategorySpinnerAdapter(@NonNull Context context, @NonNull List<String> categories) {
        super(context, R.layout.spinner_category_item, categories);
        this.context = context;
        this.categories = categories;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent, R.layout.spinner_category_item);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent, R.layout.spinner_category_dropdown_item);
    }

    private View createView(int position, View convertView, ViewGroup parent, int layoutResource) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(layoutResource, parent, false);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.ivCategoryIcon);
            holder.name = convertView.findViewById(R.id.tvCategoryName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String category = categories.get(position);
        holder.name.setText(category);
        holder.icon.setImageResource(getCategoryIcon(category));

        return convertView;
    }

    private int getCategoryIcon(String category) {
        switch (category) {
            case "All Categories":
                return R.drawable.menu;              // Menu icon for all categories
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

    private static class ViewHolder {
        ImageView icon;
        TextView name;
    }
}

