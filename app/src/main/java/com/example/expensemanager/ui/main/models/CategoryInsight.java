package com.example.expensemanager.ui.main.models;

/**
 * Model class for category insights
 */
public class CategoryInsight {
    private String categoryName;
    private double totalAmount;
    private int categoryColor;
    private double percentageChange;
    private boolean isIncrease;

    public CategoryInsight(String categoryName, double totalAmount) {
        this.categoryName = categoryName;
        this.totalAmount = totalAmount;
    }

    public CategoryInsight(String categoryName, double totalAmount, int categoryColor) {
        this.categoryName = categoryName;
        this.totalAmount = totalAmount;
        this.categoryColor = categoryColor;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(int categoryColor) {
        this.categoryColor = categoryColor;
    }

    public double getPercentageChange() {
        return percentageChange;
    }

    public void setPercentageChange(double percentageChange) {
        this.percentageChange = percentageChange;
    }

    public boolean isIncrease() {
        return isIncrease;
    }

    public void setIncrease(boolean increase) {
        isIncrease = increase;
    }
}

