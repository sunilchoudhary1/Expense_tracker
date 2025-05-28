package com.example.smartspend.model;

public class Expense {
    public int id;
    public double amount;
    public String category;
    public String date;
    public String note;

    public Expense(int id, double amount, String category, String date, String note) {
        this.id = id;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.note = note;
    }

    @Override
    public String toString() {
        return "₹" + amount + "\n" +
                "Category: " + category + "\n" +
                "Date: " + date + "\n" +
                "Note: " + note;
    }
}
