package com.example.budgettracker;

public class Transaction {
    private int id;
    private String category;
    private double amount;
    private String date;

    // UPDATED CONSTRUCTOR: Now accepts 'id' as the first parameter
    public Transaction(int id, String category, double amount, String date) {
        this.id = id;
        this.category = category;
        this.amount = amount;
        this.date = date;
    }

    // GETTER FOR ID: This fixes the "Cannot resolve method 'getId'" error
    public int getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }
}