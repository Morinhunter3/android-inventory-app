package com.example.projecttwo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


public class LoginActivity extends AppCompatActivity {

    private EditText editUsername, editPassword;
    private Button buttonLogin, buttonCreateAccount;

    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authService = new AuthService(this);

        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);

        buttonLogin.setOnClickListener(v -> {
            String username = editUsername.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter username and password.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (authService.login(username, password)) {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                goToMain();
            } else {
                Toast.makeText(this, "Invalid login. Try creating an account.", Toast.LENGTH_SHORT).show();
            }
        });

        buttonCreateAccount.setOnClickListener(v -> {
            String username = editUsername.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter username and password.", Toast.LENGTH_SHORT).show();
                return;
            }

            String passwordError = authService.validatePassword(password);
            if (passwordError != null) {
                Toast.makeText(this, passwordError, Toast.LENGTH_SHORT).show();
                return;
            }

            if (authService.userExists(username)) {
                Toast.makeText(this, "User already exists. Please log in.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean created = authService.createAccount(username, password);
            if (created) {
                Toast.makeText(this, "Account created! Logging in...", Toast.LENGTH_SHORT).show();
                goToMain();
            } else {
                Toast.makeText(this, "Account creation failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}