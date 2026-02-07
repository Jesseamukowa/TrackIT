package com.example.budgettracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Trackit.db";
    private static final int DATABASE_VERSION = 6;

    // Table Names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_CATEGORIES = "categories";
    public static final String TABLE_EXPENSES = "expenses";
    public static final String TABLE_INCOME = "monthly_income";

    // Common Column Names
    public static final String KEY_ID = "id";

    // Categories Columns
    public static final String KEY_CAT_NAME = "name";

    // Expenses Columns
    public static final String KEY_EXP_AMOUNT = "amount";
    public static final String KEY_EXP_DESC = "description";
    public static final String KEY_EXP_DATE = "date";
    public static final String KEY_EXP_CAT_ID = "category_id";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Users Table
        db.execSQL("CREATE TABLE " + TABLE_USERS + "("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT UNIQUE,"
                + "passcode TEXT,"
                + "security_answer TEXT,"
                + "profile_image TEXT" + ")");

        // 2. Categories Table
        db.execSQL("CREATE TABLE " + TABLE_CATEGORIES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_CAT_NAME + " TEXT NOT NULL,"
                + "budget_limit REAL DEFAULT 0" + ")");

        // 3. Expenses Table
        db.execSQL("CREATE TABLE " + TABLE_EXPENSES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_EXP_AMOUNT + " REAL NOT NULL,"
                + KEY_EXP_DESC + " TEXT,"
                + KEY_EXP_DATE + " TEXT,"
                + KEY_EXP_CAT_ID + " INTEGER,"
                + "FOREIGN KEY(" + KEY_EXP_CAT_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + KEY_ID + ")" + ")");

        // 4. Monthly Income Table
        db.execSQL("CREATE TABLE " + TABLE_INCOME + " (month_text TEXT PRIMARY KEY, amount REAL)");

        seedCategories(db);
    }

    private void seedCategories(SQLiteDatabase db) {
        String[] categories = {"Food", "Shopping", "Transport", "Entertainment", "Education", "Health", "Social", "Beauty"};
        for (String cat : categories) {
            ContentValues values = new ContentValues();
            values.put(KEY_CAT_NAME, cat);
            db.insert(TABLE_CATEGORIES, null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INCOME);
        onCreate(db);
    }

    // --- USER FEATURES ---

    public long addUser(String user, String pass, String answer, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", user);
        values.put("passcode", pass);
        values.put("security_answer", answer);
        values.put("profile_image", imageUri);
        return db.insert(TABLE_USERS, null, values);
    }

    public Cursor getUserData(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE username = ?", new String[]{username});
    }

    public boolean checkLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE username = ? AND passcode = ?", new String[]{username, password});
        boolean isValid = cursor.getCount() > 0;
        cursor.close();
        return isValid;
    }

    // --- INCOME FEATURES ---

    public void updateMonthlyIncome(String month, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("month_text", month);
        values.put("amount", amount);
        db.insertWithOnConflict(TABLE_INCOME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public double getTotalIncome() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(amount) FROM " + TABLE_INCOME, null);
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    // --- EXPENSE FEATURES ---

    public long addExpense(double amount, String desc, String date, int catId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_EXP_AMOUNT, amount);
        values.put(KEY_EXP_DESC, desc);
        values.put(KEY_EXP_DATE, date);
        values.put(KEY_EXP_CAT_ID, catId);
        return db.insert(TABLE_EXPENSES, null, values);
    }

    public double getTotalExpenses() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + KEY_EXP_AMOUNT + ") FROM " + TABLE_EXPENSES, null);
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    public List<Transaction> getAllTransactionsList() {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT e." + KEY_ID + ", e." + KEY_EXP_AMOUNT + ", e." + KEY_EXP_DATE + ", c." + KEY_CAT_NAME +
                " FROM " + TABLE_EXPENSES + " e " +
                " JOIN " + TABLE_CATEGORIES + " c ON e." + KEY_EXP_CAT_ID + " = c." + KEY_ID +
                " ORDER BY e." + KEY_ID + " DESC";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new Transaction(cursor.getInt(0), cursor.getString(3), cursor.getDouble(1), cursor.getString(2)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void deleteExpense(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EXPENSES, KEY_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // --- LIFE-TO-DATE (LTD) & REPORT FEATURES ---

    public double getLTDSavings() {
        return getTotalIncome() - getTotalExpenses();
    }

    public String getTopSpendingCategory() {
        SQLiteDatabase db = this.getReadableDatabase();
        String topCategory = "None";
        // Fixed: JOIN with categories table to get the name string
        String query = "SELECT c." + KEY_CAT_NAME +
                " FROM " + TABLE_EXPENSES + " e " +
                " JOIN " + TABLE_CATEGORIES + " c ON e." + KEY_EXP_CAT_ID + " = c." + KEY_ID +
                " GROUP BY e." + KEY_EXP_CAT_ID +
                " ORDER BY SUM(e." + KEY_EXP_AMOUNT + ") DESC LIMIT 1";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()) {
            topCategory = cursor.getString(0);
            cursor.close();
        }
        return topCategory;
    }

    public Cursor getCategoryReport() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT c." + KEY_CAT_NAME + ", " +
                "SUM(e." + KEY_EXP_AMOUNT + ") as total, " +
                "COUNT(e." + KEY_ID + ") as trans_count " +
                "FROM " + TABLE_EXPENSES + " e " +
                "JOIN " + TABLE_CATEGORIES + " c ON e." + KEY_EXP_CAT_ID + " = c." + KEY_ID +
                " GROUP BY c." + KEY_ID;
        return db.rawQuery(query, null);
    }

    public Cursor getAllCategories() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_CATEGORIES, null);
    }

    public Cursor getCategoryReportByMonth(String month) {
        SQLiteDatabase db = this.getReadableDatabase();

        // We use 'LIKE' so that if the date is "Jan 28, 2026", a search for "Jan" finds it
        String query = "SELECT c." + KEY_CAT_NAME + ", " +
                "SUM(e." + KEY_EXP_AMOUNT + ") as total, " +
                "COUNT(e." + KEY_ID + ") as trans_count " +
                "FROM " + TABLE_EXPENSES + " e " +
                "JOIN " + TABLE_CATEGORIES + " c ON e." + KEY_EXP_CAT_ID + " = c." + KEY_ID +
                " WHERE e." + KEY_EXP_DATE + " LIKE ? " +
                " GROUP BY c." + KEY_ID;

        // The % symbols are wildcards for the SQL LIKE operator
        return db.rawQuery(query, new String[]{"%" + month + "%"});
    }

    // 1. Method to check if the username and answer match
    public boolean validateRecovery(String username, String answer) {
        SQLiteDatabase db = this.getReadableDatabase();
        // We query the users table for a row where BOTH username and answer match
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS +
                        " WHERE username = ? AND security_answer = ?",
                new String[]{username, answer});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // 2. Method to update the password once validated
    public boolean resetPassword(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("passcode", newPassword);

        // Updates only the row belonging to that specific user
        int result = db.update(TABLE_USERS, values, "username = ?", new String[]{username});
        return result > 0;
    }
}