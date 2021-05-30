package com.group2.bank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String TAG = MainActivity.class.getSimpleName();

    private TextInputLayout userLayout, passwordLayout;
    private TextInputEditText usernameEditText, passwordEditText;
    private String username, password = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_session_info), MODE_PRIVATE);
        sharedPref.edit()
                .clear()
                .commit();

        try {
            Bundle bundle = getIntent().getExtras();
        }
        catch (NullPointerException e) {
            if (savedInstanceState != null) {

            }
            else {
                Log.e(TAG, e.getMessage());
            }
        }

        // switch to RegisterActivity on "register" button press
        final Button registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(v -> {

            Intent outgoingIntent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(outgoingIntent);

        });

        userLayout = findViewById(R.id.layout);
        passwordLayout = findViewById(R.id.layout2);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);

        // switch to LoggedInActivity on "login" button press if the username and the password are verified
        final Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(v -> {

            if (isVerified()) {
                Intent outgoingIntent = new Intent(MainActivity.this, LoggedInActivity.class);
                startActivity(outgoingIntent);
            }
            else {
                userLayout.setError(getString(R.string.username_incorrect));
                passwordLayout.setError(getString(R.string.password_incorrect));
            }
        });

        //clear the error if input has been edited
        usernameEditText.setOnEditorActionListener((v, actionId, event) -> {
            userLayout.setError(null);
            return false;
        });
        //force to be all lowercase
        usernameEditText.setFilters(new InputFilter[]{new InputFilter.AllCaps() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                return source.toString().toLowerCase();
            }
        }});

        //clear the error if input has been edited
        //try to login when the enter key is pressed
        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            passwordLayout.setError(null);

            if (actionId == EditorInfo.IME_ACTION_DONE) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);
                loginButton.callOnClick();
                return true;
            }
            return false;
        });
        //force to be all lowercase
        passwordEditText.setFilters(new InputFilter[]{new InputFilter.AllCaps() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                return source.toString().toLowerCase();
            }
        }});

        if (savedInstanceState != null) {
            username = savedInstanceState.getString(USERNAME);
            password = savedInstanceState.getString(PASSWORD);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        retrieveInputs();
        outState.putString(USERNAME, username);
        outState.putString(PASSWORD, password);
    }

    @Override
    public void onBackPressed() {
    }

    /**
     * Retrieve the username and the password inputted
     */
    private void retrieveInputs() {
        username = Objects.requireNonNull(usernameEditText.getText()).toString();
        password = Objects.requireNonNull(passwordEditText.getText()).toString();
    }

    /**
     * Verifies that the username exists, the password is correct, and each input is valid
     *
     * @return true if the username exists and the password matches; otherwise, false
     */
    private boolean isVerified() {
        retrieveInputs();
        if (username.trim().isEmpty()) return false;

        // validate username
        boolean check = Authentication.isValid(false, username, this);

        // validate password
        if (!Authentication.isValid(false, password, this)) {
            check = false;
        }

        final String query = String.format("SELECT * FROM %s WHERE %s = ? AND %s = ?; ",
                DatabaseHelper.ACCOUNTS_TABLE,
                DatabaseHelper.USERNAME_COL,
                DatabaseHelper.PASSWORD_COL);

        try (DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
             SQLiteDatabase db = dbHelper.getReadableDatabase()) {

            try (Cursor cursor = db.rawQuery(query, new String[]{username, Authentication.SHA256(password)})) {
                if (cursor.moveToFirst()) {
                    saveSessionInfo(cursor.getString(cursor.getColumnIndex(DatabaseHelper.USERNAME_COL)),
                            cursor.getString(cursor.getColumnIndex(DatabaseHelper.BALANCE_COL)));
                }
                else {
                    check = false;
                }
            }
            catch (SQLiteException e) {
                check = false;
            }
        }
        catch (SQLiteException e) {
            check = false;
            Log.e(TAG, e.getMessage());
        }

        return check;
    }

    /**
     * Saves the username and the password to a private shared preference.
     *
     * @param username
     * @param balance
     */
    private void saveSessionInfo(String username, String balance) {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_session_info), MODE_PRIVATE);
        sharedPref.edit()
                .putString(DatabaseHelper.USERNAME_COL, username)
                .putString(DatabaseHelper.BALANCE_COL, balance)
                .apply();
    }
}