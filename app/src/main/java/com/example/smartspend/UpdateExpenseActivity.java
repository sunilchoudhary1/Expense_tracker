package com.example.smartspend;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class UpdateExpenseActivity extends AppCompatActivity {

    private EditText editAmount, editCategory, editDate, editNote;
    private Button btnUpdate;
    private Calendar calendar;
    private DBHelper dbHelper;

    private int expenseId = -1; // For identifying the expense to update

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_expense);

        editAmount = findViewById(R.id.editAmount);
        editCategory = findViewById(R.id.editCategory);
        editDate = findViewById(R.id.editDate);
        editNote = findViewById(R.id.editNote);
        btnUpdate = findViewById(R.id.btnUpdate);

        dbHelper = new DBHelper(this);
        calendar = Calendar.getInstance();

        // Set date picker on editDate click
        editDate.setOnClickListener(v -> showDatePicker());

        // Get data from intent (sent from ExpenseListActivity or similar)
        if (getIntent() != null) {
            expenseId = getIntent().getIntExtra("expense_id", -1); // <-- Corrected key here
            editAmount.setText(String.valueOf(getIntent().getDoubleExtra("amount", 0)));
            editCategory.setText(getIntent().getStringExtra("category"));
            editDate.setText(getIntent().getStringExtra("date"));
            editNote.setText(getIntent().getStringExtra("note"));
        }

        Log.d("UpdateExpense", "Updating expense with id=" + expenseId);

        btnUpdate.setOnClickListener(v -> updateExpense());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    editDate.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void updateExpense() {
        String amountStr = editAmount.getText().toString().trim();
        String category = editCategory.getText().toString().trim();
        String date = editDate.getText().toString().trim();
        String note = editNote.getText().toString().trim();

        if (amountStr.isEmpty() || category.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("amount", amount);
        values.put("category", category);
        values.put("date", date);
        values.put("note", note);

        int rowsAffected = db.update("expenses", values, "id=?", new String[]{String.valueOf(expenseId)});
        db.close();

        if (rowsAffected > 0) {
            Toast.makeText(this, "Expense updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to update expense", Toast.LENGTH_SHORT).show();
        }
    }
}
