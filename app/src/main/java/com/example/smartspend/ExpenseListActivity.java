package com.example.smartspend;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartspend.model.Expense;

import java.util.ArrayList;
import java.util.Locale;

public class ExpenseListActivity extends AppCompatActivity {

    ListView listView;
    TextView totalExpenseText;
    DBHelper dbHelper;
    ArrayList<Expense> expenseList;
    ArrayAdapter<Expense> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_list);

        listView = findViewById(R.id.listViewExpenses);
        totalExpenseText = findViewById(R.id.totalExpenseText);
        dbHelper = new DBHelper(this);

        loadExpenses();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Expense selectedExpense = expenseList.get(position);
            Intent intent = new Intent(ExpenseListActivity.this, UpdateExpenseActivity.class);
            intent.putExtra("expense_id", selectedExpense.id);
            intent.putExtra("amount", selectedExpense.amount);
            intent.putExtra("category", selectedExpense.category);
            intent.putExtra("date", selectedExpense.date);
            intent.putExtra("note", selectedExpense.note);
            startActivity(intent);
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Expense selectedExpense = expenseList.get(position);

            new AlertDialog.Builder(this)
                    .setTitle("Delete Expense")
                    .setMessage("Are you sure you want to delete this expense?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        boolean deleted = dbHelper.deleteExpense(selectedExpense.id);
                        if (deleted) {
                            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                            loadExpenses(); // refresh list
                        } else {
                            Toast.makeText(this, "Error deleting", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();

            return true;
        });
    }

    private void loadExpenses() {
        expenseList = dbHelper.fetchAllExpenses();

        if (expenseList.isEmpty()) {
            totalExpenseText.setText("Total Expense: ₹0.00");
            Toast.makeText(this, "No expenses found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate total
        double total = 0;
        for (Expense e : expenseList) {
            total += e.amount;
        }
        totalExpenseText.setText(String.format(Locale.getDefault(), "Total Expense: ₹%.2f", total));

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, expenseList);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExpenses(); // Refresh list when coming back from update screen
    }
}
