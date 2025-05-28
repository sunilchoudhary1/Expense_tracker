package com.example.smartspend;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartspend.model.Expense;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "SmartSpend.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_NAME = "expenses";
    public static final String COL_ID = "id";
    public static final String COL_AMOUNT = "amount";
    public static final String COL_CATEGORY = "category";
    public static final String COL_DATE = "date";
    public static final String COL_NOTE = "note";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_AMOUNT + " REAL, " +
                COL_CATEGORY + " TEXT, " +
                COL_DATE + " TEXT, " +
                COL_NOTE + " TEXT)";
        db.execSQL(createTable);
        db.execSQL("CREATE TABLE IF NOT EXISTS expenses (id INTEGER PRIMARY KEY AUTOINCREMENT, amount REAL, category TEXT, date TEXT, note TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS budget (id INTEGER PRIMARY KEY AUTOINCREMENT, amount REAL, targetDate TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS budget (id INTEGER PRIMARY KEY AUTOINCREMENT, amount REAL, targetDate TEXT)");
        }

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertExpense(double amount, String category, String date, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_AMOUNT, amount);
        values.put(COL_CATEGORY, category);
        values.put(COL_DATE, date);
        values.put(COL_NOTE, note);

        long result = db.insert(TABLE_NAME, null, values);
        return result != -1;
    }
    public boolean deleteExpense(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_NAME, "id = ?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    public ArrayList<Expense> fetchAllExpenses() {
        ArrayList<Expense> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                double amount = cursor.getDouble(1);
                String category = cursor.getString(2);
                String date = cursor.getString(3);
                String note = cursor.getString(4);

                list.add(new Expense(id, amount, category, date, note));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    public double getMonthlyAveragePerDay(String monthYear) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Total amount for the month
        String sumQuery = "SELECT SUM(" + COL_AMOUNT + ") FROM " + TABLE_NAME + " WHERE " + COL_DATE + " LIKE ?";
        Cursor sumCursor = db.rawQuery(sumQuery, new String[]{ monthYear + "%" });

        double total = 0;
        if (sumCursor.moveToFirst()) {
            total = sumCursor.getDouble(0);
        }
        sumCursor.close();

        // Unique days in the month (e.g., 2024-05-01, 2024-05-02,...)
        String countQuery = "SELECT COUNT(DISTINCT " + COL_DATE + ") FROM " + TABLE_NAME + " WHERE " + COL_DATE + " LIKE ?";
        Cursor countCursor = db.rawQuery(countQuery, new String[]{ monthYear + "%" });

        int days = 1; // Avoid division by zero
        if (countCursor.moveToFirst()) {
            days = countCursor.getInt(0);
        }
        countCursor.close();

        return days > 0 ? total / days : 0;
    }


    public boolean updateExpense(int id, double amount, String category, String date, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("amount", amount);
        cv.put("category", category);
        cv.put("date", date);
        cv.put("note", note);

        long result = db.update(TABLE_NAME, cv, "id=?", new String[]{String.valueOf(id)});
        return result != -1;
    }

    public double getMonthlyTotal(String monthYear) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(amount) FROM " + TABLE_NAME + " WHERE date LIKE ?";
        Cursor cursor = db.rawQuery(query, new String[]{ monthYear + "%" });

        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    // Insert or Update Budget
    public boolean insertOrUpdateBudget(double amount, String targetDate) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM budget", null);
        boolean exists = cursor.moveToFirst();
        cursor.close();

        ContentValues cv = new ContentValues();
        cv.put("amount", amount);
        cv.put("targetDate", targetDate);

        long result;
        if (exists) {
            result = db.update("budget", cv, null, null);
        } else {
            result = db.insert("budget", null, cv);
        }

        return result != -1;
    }

    // Get Budget
    public Cursor getBudget() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT amount, targetDate FROM budget LIMIT 1", null);
    }


    public Cursor getAllExpenses() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_DATE + " DESC", null);
    }
}

