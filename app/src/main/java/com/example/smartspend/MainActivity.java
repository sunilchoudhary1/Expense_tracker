package com.example.smartspend;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText editBudget, editTargetDate;
    private Button btnSaveBudget, btnAddExpense, btnMonthlySummary;
    private TextView txtAvgPerDay;

    private SharedPreferences preferences;
    private static final String PREFS_NAME = "SmartSpendPrefs";
    private static final String KEY_BUDGET = "budget";
    private static final String KEY_TARGET_DATE = "target_date";

    private Calendar targetCalendar;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private boolean isEditing = true; // Initially in editing mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editBudget = findViewById(R.id.editBudget);
        editTargetDate = findViewById(R.id.editTargetDate);
        btnSaveBudget = findViewById(R.id.btnSaveBudget);
        btnAddExpense = findViewById(R.id.btnAddExpense);
        btnMonthlySummary = findViewById(R.id.btnMonthlySummary);
        txtAvgPerDay = findViewById(R.id.txtAvgPerDay);

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        targetCalendar = Calendar.getInstance();

        loadSavedBudgetAndDate();

        editTargetDate.setOnClickListener(v -> showDatePicker());

        btnSaveBudget.setOnClickListener(v -> {
            if (isEditing) {
                saveBudgetData();
                hideInputs();
                btnSaveBudget.setText("Edit Budget");
                isEditing = false;
            } else {
                showInputs();
                btnSaveBudget.setText("Save Budget");
                isEditing = true;
            }
        });

        btnAddExpense.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
            startActivity(intent);
        });

        btnMonthlySummary.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ExpenseListActivity.class);
            startActivity(intent);
        });
    }

    private void showDatePicker() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            targetCalendar.set(year, month, dayOfMonth);
            editTargetDate.setText(dateFormat.format(targetCalendar.getTime()));
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void saveBudgetData() {
        String budgetStr = editBudget.getText().toString().trim();
        String dateStr = editTargetDate.getText().toString().trim();

        if (budgetStr.isEmpty() || dateStr.isEmpty()) {
            Toast.makeText(this, "Enter both pocket money and target date", Toast.LENGTH_SHORT).show();
            return;
        }

        double budget = Double.parseDouble(budgetStr);

        preferences.edit()
                .putFloat(KEY_BUDGET, (float) budget)
                .putString(KEY_TARGET_DATE, dateStr)
                .apply();

        calculateAndShowDailyBudget(budget, targetCalendar.getTimeInMillis());

        Toast.makeText(this, "Budget saved", Toast.LENGTH_SHORT).show();
    }

    private void loadSavedBudgetAndDate() {
        float savedBudget = preferences.getFloat(KEY_BUDGET, 0);
        String savedDate = preferences.getString(KEY_TARGET_DATE, "");

        if (savedBudget > 0 && !savedDate.isEmpty()) {
            editBudget.setText(String.valueOf(savedBudget));
            editTargetDate.setText(savedDate);

            try {
                targetCalendar.setTime(dateFormat.parse(savedDate));
            } catch (Exception e) {
                e.printStackTrace();
            }

            calculateAndShowDailyBudget(savedBudget, targetCalendar.getTimeInMillis());

            // Hide inputs and set to "Edit Budget" mode
            hideInputs();
            btnSaveBudget.setText("Edit Budget");
            isEditing = false;
        } else {
            // If nothing saved yet, show inputs
            showInputs();
            btnSaveBudget.setText("Save Budget");
            isEditing = true;
        }
    }

    private void calculateAndShowDailyBudget(double totalBudget, long targetMillis) {
        long currentMillis = System.currentTimeMillis();
        long diffInMillis = targetMillis - currentMillis;
        long daysLeft = diffInMillis / (1000 * 60 * 60 * 24);

        if (daysLeft <= 0) {
            txtAvgPerDay.setText("Target date passed!");
        } else {
            double avg = totalBudget / daysLeft;
            txtAvgPerDay.setText(String.format(Locale.getDefault(), "Average per day: ₹%.2f", avg));
        }
    }

    private void hideInputs() {
        editBudget.setVisibility(View.GONE);
        editTargetDate.setVisibility(View.GONE);
    }

    private void showInputs() {
        editBudget.setVisibility(View.VISIBLE);
        editTargetDate.setVisibility(View.VISIBLE);
    }
}
