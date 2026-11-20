
package com.codegnan.app.javaapp.expenseTracker;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExpenseService {

    private final ArrayList<Expense> expenses;
    private final Budget budget;
    private final FileManager fileManager;

    private final String[] categories = {
        "Food", "Travel", "Study", "Other"
    };

    private int nextId = 1;

    public ExpenseService(
            FileManager fileManager,
            List<Expense> loadedExpenses,
            int loadedBudget) {

        this.fileManager = fileManager;
        this.expenses = new ArrayList<>(loadedExpenses);
        this.budget = new Budget(loadedBudget);

        for (Expense expense : expenses) {
            if (expense.getId() >= nextId) {
                if (expense.getId() == Integer.MAX_VALUE) {
                    throw new IllegalStateException("No more IDs available.");
                }
                nextId = expense.getId() + 1;
            }
        }
    }

    public void addExpense(
            LocalDate date,
            String category,
            int amount,
            String payment,
            String note) throws IOException {

        if (date == null || !isValidCategory(category)
                || amount < 1 || amount > 1000000
                || note == null || note.isBlank()
                || note.contains("|")
                || note.contains("\n")
                || note.contains("\r")) {

            throw new IllegalArgumentException("Invalid expense details.");
        }

        if (!payment.equals("CASH") && !payment.equals("ONLINE")) {
            throw new IllegalArgumentException("Invalid payment mode.");
        }

        if (nextId == Integer.MAX_VALUE) {
            throw new IllegalStateException("No more IDs available.");
        }

        Expense expense;

        if (payment.equals("CASH")) {
            expense = new CashExpense(
                nextId, date, category, amount, note
            );
        } else {
            expense = new OnlineExpense(
                nextId, date, category, amount, note
            );
        }

        ArrayList<Expense> updated = new ArrayList<>(expenses);
        updated.add(expense);

        // Save first. Update memory only after successful saving.
        fileManager.saveExpenses(updated);

        expenses.add(expense);
        nextId++;
    }

    public List<Expense> getExpenses() {
        return new ArrayList<>(expenses);
    }

    public List<Expense> filterByCategory(String category) {

        ArrayList<Expense> result = new ArrayList<>();

        for (Expense expense : expenses) {
            if (expense.getCategory().equals(category)) {
                result.add(expense);
            }
        }

        return result;
    }

    public long[] getTotals() {

        long[] totals = new long[categories.length];

        for (Expense expense : expenses) {

            for (int i = 0; i < categories.length; i++) {

                if (categories[i].equals(expense.getCategory())) {
                    totals[i] += expense.getAmountRupees();
                }
            }
        }

        return totals;
    }

    public Set<String> getUsedCategories() {

        HashSet<String> usedCategories = new HashSet<>();

        for (Expense expense : expenses) {
            usedCategories.add(expense.getCategory());
        }

        return usedCategories;
    }

    public long getTotalSpending() {

        long total = 0;

        for (Expense expense : expenses) {
            total += expense.getAmountRupees();
        }

        return total;
    }

    public Budget getBudget() {
        return budget;
    }

    public void updateBudget(int amount) throws IOException {

        Budget newBudget = new Budget(amount);

        // Save first. Change the current budget only after saving.
        fileManager.saveBudget(newBudget.getAmountRupees());

        budget.setAmountRupees(newBudget.getAmountRupees());
    }

    public void saveReport() throws IOException {

        fileManager.saveReport(
            expenses,
            budget.getAmountRupees(),
            getTotals(),
            getUsedCategories().size()
        );
    }

    public void showSummary() {

        long total = getTotalSpending();
        long remaining = (long) budget.getAmountRupees() - total;

        System.out.println("\n===== SPENDING SUMMARY =====");
        System.out.println("Expense count: " + expenses.size());
        System.out.println("Total spending: " + total + " rupees");
        System.out.println(
            "Budget: " + budget.getAmountRupees() + " rupees"
        );

        if (remaining >= 0) {
            System.out.println("Remaining: " + remaining + " rupees");
        } else {
            System.out.println("Over budget by: " + (-remaining) + " rupees");
        }

        long[] totals = getTotals();

        for (int i = 0; i < categories.length; i++) {
            System.out.println(categories[i] + ": " + totals[i] + " rupees");
        }

        System.out.println(
            "Categories used: " + getUsedCategories().size()
        );
    }

    private boolean isValidCategory(String category) {

        for (String valid : categories) {
            if (valid.equals(category)) {
                return true;
            }
        }

        return false;
    }
}
