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
    private static final int DB_VERSION = 1;

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
                        COL_ITEM_QTY + " INTEGER NOT NULL DEFAULT 0" +
                        ");";

        db.execSQL(createUsers);
        db.execSQL(createItems);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(db);
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
        public long addItem(String name, int quantity) {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_ITEM_NAME, name);
            values.put(COL_ITEM_QTY, quantity);
            return db.insert(TABLE_ITEMS, null, values);
        }

    public Cursor getAllItems() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_ITEMS,
                null,
                null, null, null, null,
                COL_ITEM_NAME + " ASC");
    }

    public boolean updateItem(long id, String name, int quantity) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ITEM_NAME, name);
        values.put(COL_ITEM_QTY, quantity);

        int rows = db.update(TABLE_ITEMS, values, COL_ITEM_ID + "=?",
                new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public boolean deleteItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_ITEMS, COL_ITEM_ID + "=?",
                new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public Cursor getItemById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_ITEMS,
                null,
                COL_ITEM_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);
    }
}
