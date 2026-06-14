package com.example.projecttwo;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.projecttwo.SmsUtil;

import androidx.appcompat.app.AppCompatActivity;

public class AddEditItemActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "extra_item_id";

    private DatabaseHelper db;

    private TextView textTitle;
    private EditText editItemName;
    private EditText editItemQty;
    private Button buttonSave, buttonDelete;

    private long editingItemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_item);

        db = new DatabaseHelper(this);

        textTitle = findViewById(R.id.textTitle);
        editItemName = findViewById(R.id.editItemName);
        editItemQty = findViewById(R.id.editItemQty);
        buttonSave = findViewById(R.id.buttonSave);
        buttonDelete = findViewById(R.id.buttonDelete);

        // Check if we are editing an existing item
        if (getIntent() != null && getIntent().hasExtra(EXTRA_ITEM_ID)) {
            editingItemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        }

        if (editingItemId != -1) {
            textTitle.setText("Edit Item");
            buttonDelete.setVisibility(Button.VISIBLE);
            loadItem(editingItemId);
        } else {
            textTitle.setText("Add Item");
        }

        buttonSave.setOnClickListener(v -> saveItem());
        buttonDelete.setOnClickListener(v -> deleteItem());
    }

    private void loadItem(long id) {
        Cursor cursor = db.getItemById(id);
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ITEM_NAME));
            int qty = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ITEM_QTY));

            editItemName.setText(name);
            editItemQty.setText(String.valueOf(qty));
        }
        if (cursor != null) cursor.close();
    }

    private void saveItem() {
        String name = editItemName.getText().toString().trim();
        String qtyText = editItemQty.getText().toString().trim();


        int qty;
        try {
            qty = Integer.parseInt(qtyText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Quantity must be a number.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.length() > 40) {
            Toast.makeText(this, "Item name must be 40 characters or less.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (qty < 0) {
            Toast.makeText(this, "Quantity cannot be negative.", Toast.LENGTH_SHORT).show();
            return;
        }

        String category = "General";
        int minimumStock = 10;

        if (editingItemId == -1) {
            long id = db.addItem(name, qty, category, minimumStock);
            if (id != -1) {


                if (qty <= 10) {
                    SmsUtil.sendLowStockAlert(this, name, qty);
                }

                Toast.makeText(this, "Item added!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Add failed.", Toast.LENGTH_SHORT).show();
            }


        } else {
            boolean updated = db.updateItem(editingItemId, name, qty, category, minimumStock);
            if (updated) {


                if (qty <= 10) {
                    SmsUtil.sendLowStockAlert(this, name, qty);
                }

                Toast.makeText(this, "Item updated!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Update failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void deleteItem() {
        if (editingItemId == -1) return;

        boolean deleted = db.deleteItem(editingItemId);
        if (deleted) {
            Toast.makeText(this, "Item deleted!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Delete failed.", Toast.LENGTH_SHORT).show();
        }
    }
}

