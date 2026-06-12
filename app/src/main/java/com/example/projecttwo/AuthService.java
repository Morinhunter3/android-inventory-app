package com.example.projecttwo;

import android.content.Context;

public class AuthService {

    private final DatabaseHelper db;

    public AuthService(Context context) {
        db = new DatabaseHelper(context);
    }

    public String validatePassword(String password) {
        if (password.length() < 8) {
            return "Password must be at least 8 characters.";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Password must include an uppercase letter.";
        }
        if (!password.matches(".*[0-9].*")) {
            return "Password must include a number.";
        }
        return null;
    }

    public boolean createAccount(String username, String password) {
        return db.createUser(username, password);
    }

    public boolean login(String username, String password) {
        return db.validateUser(username, password);
    }

    public boolean userExists(String username) {
        return db.userExists(username);
    }
}
