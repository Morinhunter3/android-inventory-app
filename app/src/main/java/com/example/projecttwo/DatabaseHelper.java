package com.example.projecttwo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;



public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "mobile2inventory.db";
    private static final int DB_VERSION = 2;

    // user tables
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";

    // inventory tables
    public static final String TABLE_ITEMS = "items";
    public static final String COL_ITEM_ID = "id";
    public static final String COL_ITEM_NAME = "name";
    public static final String COL_ITEM_QTY = "quantity";

    public static final String COL_ITEM_CATEGORY = "category";
    public static final String COL_ITEM_MIN_STOCK = "minimum_stock";
    public static final String COL_ITEM_LAST_UPDATED = "last_updated";

    public static final String TABLE_HISTORY = "inventory_history";
    public static final String COL_HISTORY_ID = "history_id";
    public static final String COL_HISTORY_ITEM_ID = "item_id";
    public static final String COL_HISTORY_ACTION = "action_type";
    public static final String COL_HISTORY_QTY = "quantity";
    public static final String COL_HISTORY_DATE = "date";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsers =
                "CREATE TABLE " + TABLE_USERS + " (" +
                        COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_USERNAME + " TEXT UNIQUE NOT NULL, " +
                        COL_PASSWORD + " TEXT NOT NULL" +
                        ");";

        String createItems =
                "CREATE TABLE " + TABLE_ITEMS + " (" +
                        COL_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_ITEM_NAME + " TEXT NOT NULL, " +
                        COL_ITEM_QTY + " INTEGER NOT NULL DEFAULT 0 CHECK(" + COL_ITEM_QTY + " >= 0), " +
                        COL_ITEM_CATEGORY + " TEXT NOT NULL DEFAULT 'General', " +
                        COL_ITEM_MIN_STOCK + " INTEGER NOT NULL DEFAULT 10 CHECK(" + COL_ITEM_MIN_STOCK + " >= 0), " +
                        COL_ITEM_LAST_UPDATED + " TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                        ");";

        String createHistoryTable =
                "CREATE TABLE " + TABLE_HISTORY + " (" +
                        COL_HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_HISTORY_ITEM_ID + " INTEGER, " +
                        COL_HISTORY_ACTION + " TEXT NOT NULL, " +
                        COL_HISTORY_QTY + " INTEGER NOT NULL, " +
                        COL_HISTORY_DATE + " TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY(" + COL_HISTORY_ITEM_ID + ") REFERENCES " +
                        TABLE_ITEMS + "(" + COL_ITEM_ID + ")" +
                        ");";

        db.execSQL(createUsers);
        db.execSQL(createItems);
        db.execSQL(createHistoryTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_ITEMS +
                    " ADD COLUMN " + COL_ITEM_CATEGORY +
                    " TEXT NOT NULL DEFAULT 'General'");

            db.execSQL("ALTER TABLE " + TABLE_ITEMS +
                    " ADD COLUMN " + COL_ITEM_MIN_STOCK +
                    " INTEGER NOT NULL DEFAULT 10");

            db.execSQL("ALTER TABLE " + TABLE_ITEMS +
                    " ADD COLUMN " + COL_ITEM_LAST_UPDATED +
                    " TEXT NOT NULL DEFAULT ''");

            String createHistoryTable = "CREATE TABLE IF NOT EXISTS " + TABLE_HISTORY + " (" +
                    COL_HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_HISTORY_ITEM_ID + " INTEGER, " +
                    COL_HISTORY_ACTION + " TEXT NOT NULL, " +
                    COL_HISTORY_QTY + " INTEGER NOT NULL, " +
                    COL_HISTORY_DATE + " TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(" + COL_HISTORY_ITEM_ID + ") REFERENCES " +
                    TABLE_ITEMS + "(" + COL_ITEM_ID + ")" +
                    ");";

            db.execSQL(createHistoryTable);
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    // user methods
    public boolean createUser(String username, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, hashPassword(password));

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean validateUser(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        String hashedPassword = hashPassword(password);
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USERNAME + "=? AND " + COL_PASSWORD + "=?",
                new String[]{username, hashedPassword},
                null, null, null);

        boolean found = cursor.moveToFirst();
        cursor.close();
        return found;
    }

    public boolean userExists(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USERNAME + "=?",
                new String[]{username},
                null, null, null);

        boolean found = cursor.moveToFirst();
        cursor.close();
        return found;
    }


        // item CRUD methods
        public long addItem(String name, int quantity, String category, int minimumStock) {
            SQLiteDatabase db = getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COL_ITEM_NAME, name);
            values.put(COL_ITEM_QTY, quantity);
            values.put(COL_ITEM_CATEGORY, category);
            values.put(COL_ITEM_MIN_STOCK, minimumStock);

            long itemId = db.insert(TABLE_ITEMS, null, values);

            if (itemId != -1) {
                logInventoryHistory(itemId, "ADD", quantity);
            }

            return itemId;
        }

    public Cursor getAllItems() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_ITEMS,
                null,
                null, null, null, null,
                COL_ITEM_NAME + " ASC");
    }

    public boolean updateItem(long id, String name, int quantity, String category, int minimumStock) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_ITEM_NAME, name);
        values.put(COL_ITEM_QTY, quantity);
        values.put(COL_ITEM_CATEGORY, category);
        values.put(COL_ITEM_MIN_STOCK, minimumStock);
        values.put(COL_ITEM_LAST_UPDATED, System.currentTimeMillis());

        int rowsUpdated = db.update(
                TABLE_ITEMS,
                values,
                COL_ITEM_ID + "=?",
                new String[]{String.valueOf(id)}
        );

        if (rowsUpdated > 0) {
            logInventoryHistory(id, "UPDATE", quantity);
        }

        return rowsUpdated > 0;
    }

    public boolean deleteItem(long id) {
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = getItemById(id);
        int quantity = 0;

        if (cursor != null && cursor.moveToFirst()) {
            quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ITEM_QTY));
            cursor.close();
        }

        logInventoryHistory(id, "DELETE", quantity);

        int rowsDeleted = db.delete(
                TABLE_ITEMS,
                COL_ITEM_ID + "=?",
                new String[]{String.valueOf(id)}
        );

        return rowsDeleted > 0;
    }

    private void logInventoryHistory(long itemId, String action, int quantity) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_HISTORY_ITEM_ID, itemId);
        values.put(COL_HISTORY_ACTION, action);
        values.put(COL_HISTORY_QTY, quantity);

        db.insert(TABLE_HISTORY, null, values);
    }

    public Cursor getItemById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_ITEMS,
                null,
                COL_ITEM_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);
    }

    public Cursor searchItems(String searchText, String filterMode) {
        SQLiteDatabase db = getReadableDatabase();

        String selection = COL_ITEM_NAME + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + searchText + "%"};

        if ("LOW".equals(filterMode)) {
            selection += " AND " + COL_ITEM_QTY + " > 0 AND " +
                    COL_ITEM_QTY + " <= " + COL_ITEM_MIN_STOCK;
        } else if ("OUT".equals(filterMode)) {
            selection += " AND " + COL_ITEM_QTY + " = 0";
        }

        return db.query(
                TABLE_ITEMS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                COL_ITEM_NAME + " ASC"
        );
    }
}
