package com.example.projecttwo;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements InventoryAdapter.InventoryListener {

    private InventoryService inventoryService;

    private RecyclerView recyclerInventory;
    private FloatingActionButton fabAddItem;

    private EditText editSearch;
    private Button buttonAllItems, buttonLowStock, buttonOutOfStock;
    private Button buttonSortName, buttonSortLow, buttonSortHigh;
    private Button buttonLogout;

    private InventoryAdapter adapter;
    private final List<InventoryItem> itemList = new ArrayList<>();

    private final Map<Long, InventoryItem> inventoryMap = new HashMap<>();
    private enum FilterMode { ALL, LOW, OUT }
    private FilterMode filterMode = FilterMode.ALL;

    private enum SortMode { NONE, NAME, QTY_LOW_HIGH, QTY_HIGH_LOW }
    private SortMode sortMode = SortMode.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button buttonSmsSettings = findViewById(R.id.buttonSmsSettings);

        if (buttonSmsSettings == null) {
            Toast.makeText(this, "buttonSmsSettings is null (wrong layout/id)", Toast.LENGTH_LONG).show();
            Log.e("MainActivity", "buttonSmsSettings is null");
        } else {
            buttonSmsSettings.setOnClickListener(v -> {
                Toast.makeText(this, "Opening SMS Settings...", Toast.LENGTH_SHORT).show();
                Log.d("MainActivity", "SMS Settings button clicked");

                Intent intent = new Intent(MainActivity.this, SmsNotificationActivity.class);
                startActivity(intent);
            });
        }

        inventoryService = new InventoryService(this);

        recyclerInventory = findViewById(R.id.recyclerInventory);
        fabAddItem = findViewById(R.id.fabAddItem);

        editSearch = findViewById(R.id.editSearch);
        buttonAllItems = findViewById(R.id.buttonAllItems);
        buttonLowStock = findViewById(R.id.buttonLowStock);
        buttonOutOfStock = findViewById(R.id.buttonOutOfStock);
        buttonLogout = findViewById(R.id.buttonLogout);
        buttonSortName = findViewById(R.id.buttonSortName);
        buttonSortLow = findViewById(R.id.buttonSortLow);
        buttonSortHigh = findViewById(R.id.buttonSortHigh);
        recyclerInventory.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new InventoryAdapter(this, itemList, this);
        recyclerInventory.setAdapter(adapter);

        loadItemsFromDb();

        fabAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditItemActivity.class);
            startActivity(intent);
        });


        // Filter buttons
        buttonAllItems.setOnClickListener(v -> {
            filterMode = FilterMode.ALL;
            loadItemsFromDb();
        });

        buttonLowStock.setOnClickListener(v -> {
            filterMode = FilterMode.LOW;
            loadItemsFromDb();
        });

        buttonOutOfStock.setOnClickListener(v -> {
            filterMode = FilterMode.OUT;
            loadItemsFromDb();
        });

        buttonSortName.setOnClickListener(v -> {

            sortMode = SortMode.NAME;
            loadItemsFromDb();
        });

        buttonSortLow.setOnClickListener(v -> {

            sortMode = SortMode.QTY_LOW_HIGH;
            loadItemsFromDb();
        });

        buttonSortHigh.setOnClickListener(v -> {

            sortMode = SortMode.QTY_HIGH_LOW;
            loadItemsFromDb();
        });

        buttonLogout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        //search reload
        editSearch.setOnEditorActionListener((v, actionId, event) -> {
            loadItemsFromDb();
            return false;
        });
    }

    private void loadItemsFromDb() {
        itemList.clear();
        inventoryMap.clear();

        String searchText = editSearch.getText().toString().trim().toLowerCase();

        Cursor cursor = inventoryService.searchItems(searchText, filterMode.name());

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ITEM_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ITEM_NAME));
                int qty = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ITEM_QTY));

                String category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ITEM_CATEGORY));
                int minimumStock = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ITEM_MIN_STOCK));
                String lastUpdated = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ITEM_LAST_UPDATED));

                InventoryItem item = new InventoryItem(id, name, qty, category, minimumStock, lastUpdated);

                itemList.add(item);
                inventoryMap.put(id, item);
            }

            cursor.close();
        }

        if (sortMode == SortMode.NAME) {
            itemList.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
        } else if (sortMode == SortMode.QTY_LOW_HIGH) {
            itemList.sort((a, b) -> Integer.compare(a.quantity, b.quantity));
        } else if (sortMode == SortMode.QTY_HIGH_LOW) {
            itemList.sort((a, b) -> Integer.compare(b.quantity, a.quantity));
        }

        if (itemList.isEmpty()) {
            Toast.makeText(this, "No inventory items found.", Toast.LENGTH_SHORT).show();
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClicked(InventoryItem item) {
        InventoryItem selectedItem = getItemById(item.id);

        if (selectedItem == null) {
            Toast.makeText(this, "Item not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, AddEditItemActivity.class);
        intent.putExtra(AddEditItemActivity.EXTRA_ITEM_ID, selectedItem.id);
        startActivity(intent);
    }
    @Override
    public void onDeleteClicked(InventoryItem item) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete " + item.name + "?")
                .setPositiveButton("Delete", (dialog, which) -> {

                    boolean deleted = inventoryService.deleteItem(item.id);

                    if (deleted) {
                        Toast.makeText(this,
                                "Deleted: " + item.name,
                                Toast.LENGTH_SHORT).show();

                        loadItemsFromDb();
                    } else {
                        Toast.makeText(this,
                                "Delete failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private InventoryItem getItemById(long id) {
        return inventoryMap.get(id);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItemsFromDb();
    }
}
