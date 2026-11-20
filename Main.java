
package com.codegnan.app.javaapp.expenseTracker;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner sc = new Scanner(System.in);

    private static final String[] CATEGORIES = {
        "Food", "Travel", "Study", "Other"
    };

    private static ExpenseService service;

    public static void main(String[] args) {

        try {
            FileManager fileManager = new FileManager();

            List<Expense> loadedExpenses =
                fileManager.loadExpenses();

            int loadedBudget = fileManager.loadBudget();

            service = new ExpenseService(
                fileManager, loadedExpenses, loadedBudget
            );

            System.out.println("Expense Tracker loaded successfully!");

            showMenu();

        } catch (IOException | IllegalArgumentException |
                 IllegalStateException e) {

            System.out.println("Application could not start.");
            System.out.println("Error: " + e.getMessage());
            System.out.println("Please correct the data files and restart.");
        }
    }

    public static void showMenu() {

        int choice;

        do {
            System.out.println("\n===== PERSONAL EXPENSE TRACKER =====");
            System.out.println("1. Add expense");
            System.out.println("2. View all expenses");
            System.out.println("3. View expenses by category");
            System.out.println("4. Set budget");
            System.out.println("5. View summary");
            System.out.println("6. Save report");
            System.out.println("0. Exit");

            choice = readInt("Enter your choice: ", 0, 6);

            try {
                switch (choice) {

                    case 1:
                        addExpense();
                        break;

                    case 2:
                        viewAllExpenses();
                        break;

                    case 3:
                        viewExpensesByCategory();
                        break;

                    case 4:
                        setBudget();
                        break;

                    case 5:
                        service.showSummary();
                        break;

                    case 6:
                        service.saveReport();
                        System.out.println("Report saved successfully!");
                        break;

                    case 0:
                        System.out.println("Thank you for using Expense Tracker!");
                        break;

                    default:
                        System.out.println("Invalid choice.");
                }

            } catch (IOException | IllegalArgumentException |
                     IllegalStateException e) {

                System.out.println("Operation failed: " + e.getMessage());
            }

        } while (choice != 0);
    }

    private static void addExpense() throws IOException {

        System.out.println("\n===== ADD EXPENSE =====");

        LocalDate date = readDate();

        System.out.println("\nCategories:");

        for (int i = 0; i < CATEGORIES.length; i++) {
            System.out.println((i + 1) + ". " + CATEGORIES[i]);
        }

        int categoryChoice =
            readInt("Select category: ", 1, CATEGORIES.length);

        String category = CATEGORIES[categoryChoice - 1];

        int amount = readInt(
            "Enter amount in whole rupees: ", 1, 1000000
        );

        System.out.println("\nPayment modes:");
        System.out.println("1. Cash");
        System.out.println("2. Online");

        int paymentChoice =
            readInt("Select payment mode: ", 1, 2);

        String payment =
            paymentChoice == 1 ? "CASH" : "ONLINE";

        String note = readNote();

        service.addExpense(date, category, amount, payment, note);

        System.out.println(
            "Expense " + (getLastExpenseId()) + " saved successfully!"
        );
    }

    private static int getLastExpenseId() {

        List<Expense> expenses = service.getExpenses();

        return expenses.get(expenses.size() - 1).getId();
    }

    private static void viewAllExpenses() {

        List<Expense> expenses = service.getExpenses();

        if (expenses.isEmpty()) {
            System.out.println("No records found.");
            return;
        }

        System.out.println("\n===== EXPENSE HISTORY =====");

        for (Expense expense : expenses) {
            displayExpense(expense);
        }
    }

    private static void viewExpensesByCategory() {

        System.out.println("\nCategories:");

        for (int i = 0; i < CATEGORIES.length; i++) {
            System.out.println((i + 1) + ". " + CATEGORIES[i]);
        }

        int choice =
            readInt("Select category: ", 1, CATEGORIES.length);

        String category = CATEGORIES[choice - 1];

        List<Expense> filtered =
            service.filterByCategory(category);

        if (filtered.isEmpty()) {
            System.out.println("No records found in " + category + ".");
            return;
        }

        System.out.println("\n===== " + category + " EXPENSES =====");

        for (Expense expense : filtered) {
            displayExpense(expense);
        }
    }

    private static void displayExpense(Expense expense) {

        System.out.println("\nID: " + expense.getId());
        System.out.println("Date: " + expense.getDate());
        System.out.println("Category: " + expense.getCategory());
        System.out.println("Amount: " + expense.getAmountRupees());
        System.out.println("Payment mode: " + expense.getPaymentMode());
        System.out.println("Note: " + expense.getNote());
        System.out.println("-------------------------");
    }

    private static void setBudget() throws IOException {

        System.out.println("\n===== SET BUDGET =====");

        int amount = readInt(
            "Enter budget in whole rupees: ", 1, 1000000
        );

        service.updateBudget(amount);

        System.out.println("Budget saved successfully!");
    }

    private static LocalDate readDate() {

        while (true) {

            System.out.print("Date (yyyy-MM-dd): ");
            String input = sc.nextLine().trim();

            try {
                return LocalDate.parse(input);

            } catch (DateTimeParseException e) {
                System.out.println(
                    "Invalid date! Please use yyyy-MM-dd format."
                );
            }
        }
    }

    private static int readInt(
            String message, int min, int max) {

        while (true) {

            System.out.print(message);
            String input = sc.nextLine().trim();

            try {
                int value = Integer.parseInt(input);

                if (value >= min && value <= max) {
                    return value;
                }

                System.out.println(
                    "Enter a value between " + min + " and " + max + "."
                );

            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Enter a whole number.");
            }
        }
    }

    private static String readNote() {

        while (true) {

            System.out.print("Short note: ");
            String note = sc.nextLine().trim();

            if (note.isBlank()) {
                System.out.println("Note cannot be empty.");
            } else if (note.contains("|") ||
                       note.contains("\n") ||
                       note.contains("\r")) {

                System.out.println(
                    "Note cannot contain | or line breaks."
                );

            } else {
                return note;
            }
        }
    }
}
