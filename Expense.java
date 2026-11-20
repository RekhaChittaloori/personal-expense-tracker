
package com.codegnan.app.javaapp.expenseTracker;

import java.time.LocalDate;

public abstract class Expense {

    private int id;
    private int amountRupees;
    private LocalDate date;
    private String category;
    private String note;

    public Expense(int id, LocalDate date, String category,
                   int amountRupees, String note) {

        this.id = id;
        this.date = date;
        this.category = category;
        this.amountRupees = amountRupees;
        this.note = note;
    }

    public int getId() {
        return id;
    }

    public int getAmountRupees() {
        return amountRupees;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public String getNote() {
        return note;
    }

    public abstract String getPaymentMode();
}
