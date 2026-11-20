
package com.codegnan.app.javaapp.expenseTracker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileManager {

    private final Path dataFolder = Path.of("data");
    private final Path expenseFile = dataFolder.resolve("expenses.txt");
    private final Path budgetFile = dataFolder.resolve("budget.txt");
    private final Path reportFile = dataFolder.resolve("report.txt");

    private final String[] categories = {
        "Food", "Travel", "Study", "Other"
    };

    public FileManager() throws IOException {
        Files.createDirectories(dataFolder);
    }

    public List<Expense> loadExpenses() throws IOException {

        List<Expense> loaded = new ArrayList<>();

        if (!Files.exists(expenseFile)) {
            return loaded;
        }

        Set<Integer> usedIds = new HashSet<>();

        try (BufferedReader reader =
                 Files.newBufferedReader(expenseFile, StandardCharsets.UTF_8)) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                try {
                    String[] fields = line.split("\\|", -1);

                    if (fields.length != 6) {
                        throw new IllegalArgumentException("Expected 6 fields.");
                    }

                    int id = Integer.parseInt(fields[0]);
                    LocalDate date = LocalDate.parse(fields[1]);
                    String category = fields[2];
                    int amount = Integer.parseInt(fields[3]);
                    String payment = fields[4];
                    String note = fields[5];

                    if (id <= 0 || !usedIds.add(id)) {
                        throw new IllegalArgumentException(
                            "Invalid or duplicate ID."
                        );
                    }

                    if (!isValidCategory(category)) {
                        throw new IllegalArgumentException(
                            "Invalid category."
                        );
                    }

                    if (amount < 1 || amount > 1000000) {
                        throw new IllegalArgumentException(
                            "Invalid amount."
                        );
                    }

                    validateText(note, "Note");

                    Expense expense;

                    if (payment.equals("CASH")) {
                        expense = new CashExpense(
                            id, date, category, amount, note
                        );
                    } else if (payment.equals("ONLINE")) {
                        expense = new OnlineExpense(
                            id, date, category, amount, note
                        );
                    } else {
                        throw new IllegalArgumentException(
                            "Invalid payment mode."
                        );
                    }

                    loaded.add(expense);

                } catch (RuntimeException e) {
                    throw new IOException(
                        "Invalid data in expenses.txt at line "
                        + lineNumber + ": " + e.getMessage(), e
                    );
                }
            }
        }

        return loaded;
    }

    public void saveExpenses(List<Expense> expenses) throws IOException {

        writeSafely(expenseFile, writer -> {

            for (Expense expense : expenses) {

                writer.write(
                    expense.getId() + "|"
                    + expense.getDate() + "|"
                    + expense.getCategory() + "|"
                    + expense.getAmountRupees() + "|"
                    + expense.getPaymentMode() + "|"
                    + expense.getNote()
                );

                writer.newLine();
            }
        });
    }

    public int loadBudget() throws IOException {

        if (!Files.exists(budgetFile)) {
            return 2000;
        }

        try (BufferedReader reader =
                 Files.newBufferedReader(budgetFile, StandardCharsets.UTF_8)) {

            String first = reader.readLine();
            String second = reader.readLine();

            try {
                if (first == null || second != null) {
                    throw new IllegalArgumentException(
                        "Budget file must contain exactly one line."
                    );
                }

                int amount = Integer.parseInt(first);

                if (amount < 1 || amount > 1000000) {
                    throw new IllegalArgumentException(
                        "Budget must be between 1 and 1000000."
                    );
                }

                return amount;

            } catch (RuntimeException e) {
                throw new IOException(
                    "Invalid data in budget.txt: " + e.getMessage(), e
                );
            }
        }
    }

    public void saveBudget(int amount) throws IOException {

        if (amount < 1 || amount > 1000000) {
            throw new IllegalArgumentException("Invalid budget.");
        }

        writeSafely(budgetFile, writer -> {
            writer.write(String.valueOf(amount));
            writer.newLine();
        });
    }

    public void saveReport(
            List<Expense> expenses,
            int budget,
            long[] totals,
            int categoriesUsed) throws IOException {

        long total = 0;

        for (long value : totals) {
            total += value;
        }

        final long reportTotal = total;

        writeSafely(reportFile, writer -> {

            writer.write("PERSONAL EXPENSE TRACKER REPORT");
            writer.newLine();

            writer.write("-------------------------------");
            writer.newLine();

            writer.write("Expense count: " + expenses.size());
            writer.newLine();

            writer.write("Total spending: " + reportTotal + " rupees");
            writer.newLine();

            writer.write("Monthly budget: " + budget + " rupees");
            writer.newLine();

            if (reportTotal > budget) {
                writer.write("Over budget by: "
                        + (reportTotal - budget) + " rupees");
            } else {
                writer.write("Remaining budget: "
                        + (budget - reportTotal) + " rupees");
            }

            writer.newLine();
            writer.newLine();

            writer.write("CATEGORY-WISE SPENDING");
            writer.newLine();

            for (int i = 0; i < categories.length; i++) {
                writer.write(categories[i] + ": " + totals[i] + " rupees");
                writer.newLine();
            }

            writer.newLine();
            writer.write("Distinct categories used: " + categoriesUsed);
            writer.newLine();

            writer.newLine();
            writer.write("EXPENSE DETAILS");
            writer.newLine();

            for (Expense expense : expenses) {
                writer.write(
                        expense.getId() + " | "
                        + expense.getDate() + " | "
                        + expense.getCategory() + " | "
                        + expense.getAmountRupees() + " | "
                        + expense.getPaymentMode() + " | "
                        + expense.getNote()
                );
                writer.newLine();
            }
        });
    }

    private boolean isValidCategory(String category) {

        for (String valid : categories) {
            if (valid.equals(category)) {
                return true;
            }
        }

        return false;
    }

    private void validateText(String value, String field) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                field + " cannot be empty."
            );
        }

        if (value.contains("|") ||
            value.contains("\n") ||
            value.contains("\r")) {

            throw new IllegalArgumentException(
                field + " cannot contain | or line breaks."
            );
        }
    }

    @FunctionalInterface
    private interface WriterAction {
        void write(BufferedWriter writer) throws IOException;
    }

    private void writeSafely(
            Path target, WriterAction action) throws IOException {

        Path temporary = Files.createTempFile(
            dataFolder, "temp-", ".txt"
        );

        try {
            try (BufferedWriter writer =
                     Files.newBufferedWriter(
                         temporary, StandardCharsets.UTF_8)) {

                action.write(writer);
            }

            try {
                Files.move(
                    temporary,
                    target,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
                );

            } catch (AtomicMoveNotSupportedException e) {

                Files.move(
                    temporary,
                    target,
                    StandardCopyOption.REPLACE_EXISTING
                );
            }

        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
