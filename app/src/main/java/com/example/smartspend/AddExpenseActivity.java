package com.example.smartspend;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AddExpenseActivity extends AppCompatActivity {

    EditText editAmount, editCategory, editDate, editNote;
    Button btnSave;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        // Initialize views
        editAmount = findViewById(R.id.editAmount);
        editCategory = findViewById(R.id.editCategory);
        editDate = findViewById(R.id.editDate);
        editNote = findViewById(R.id.editNote);
        btnSave = findViewById(R.id.btnSave);

        // Initialize database helper
        dbHelper = new DBHelper(this);

        // Date Picker
        editDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        String selectedDate = String.format("%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                        editDate.setText(selectedDate);
                    }, year, month, day);
            datePickerDialog.show();
        });

        // Save button click
        btnSave.setOnClickListener(v -> {
            String amountStr = editAmount.getText().toString().trim();
            String category = editCategory.getText().toString().trim();
            String date = editDate.getText().toString().trim();
            String note = editNote.getText().toString().trim();

            // Validate
            if (amountStr.isEmpty() || category.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean inserted = dbHelper.insertExpense(amount, category, date, note);
            if (inserted) {
                Toast.makeText(this, "Expense saved", Toast.LENGTH_SHORT).show();
                finish(); // return to previous screen
            } else {
                Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
