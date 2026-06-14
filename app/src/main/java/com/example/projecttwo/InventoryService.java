package com.example.projecttwo;

import android.content.Context;
import android.database.Cursor;

public class InventoryService {

    private final DatabaseHelper db;

    public InventoryService(Context context) {
        db = new DatabaseHelper(context);
    }

    public Cursor getAllItems() {
        return db.getAllItems();
    }

    public long addItem(String name, int quantity, String category, int minimumStock) {
        return db.addItem(name, quantity, category, minimumStock);
    }

    public boolean updateItem(long id, String name, int quantity, String category, int minimumStock) {
        return db.updateItem(id, name, quantity, category, minimumStock);
    }

    public Cursor searchItems(String searchText, String filterMode) {
        return db.searchItems(searchText, filterMode);
    }

    public boolean deleteItem(long id) {
        return db.deleteItem(id);
    }

    public Cursor getItemById(long id) {
        return db.getItemById(id);
    }
}
