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

public class MainActivity extends AppCompatActivity {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String TAG = MainActivity.class.getSimpleName();

    private TextInputEditText usernameEditText, passwordEditText;
    private String username, password = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // switch to RegisterActivity on "register" button press
        final Button registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(v -> {

            Intent outgoingIntent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(outgoingIntent);

        });

        final TextInputLayout userLayout = findViewById(R.id.layout);
        final TextInputLayout passwordLayout = findViewById(R.id.layout2);
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

    /**
     * Retrieve the username and the password inputted
     */
    private void retrieveInputs() {
        username = usernameEditText.getText().toString();
        password = passwordEditText.getText().toString();
    }

    /**
     * Verifies that the username exists and the password is correct
     *
     * check if this is case sensitive
     *
     * @return true if the username exists and the password matches; otherwise, false
     */
    private boolean isVerified() {
        retrieveInputs();
        if (username.trim().isEmpty() || password.trim().isEmpty()) return false;

        final String query = String.format("SELECT * FROM %s WHERE %s = '%s' AND %s = '%s'; ",
                DatabaseHelper.ACCOUNTS_TABLE,
                DatabaseHelper.USERNAME_COL, username,
                DatabaseHelper.PASSWORD_COL, Authentication.SHA256(password));

        System.out.println(query);

        try (DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
             SQLiteDatabase db = dbHelper.getReadableDatabase()) {

            try (Cursor cursor = db.rawQuery(query, new String[]{})) {
                if (cursor.moveToFirst()) {
                    saveSessionInfo(username, cursor.getFloat(cursor.getColumnIndex(DatabaseHelper.BALANCE_COL)));
                    return true;
                }
            }
            catch (SQLiteException e) {
            }
        }
        catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
        }

        return false;
    }

    /**
     * Saves the username and the password to a private shared preference.
     *
     * @param username
     * @param balance
     */
    private void saveSessionInfo(String username, float balance) {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_session_info), MODE_PRIVATE);
        sharedPref.edit()
                .putString(DatabaseHelper.USERNAME_COL, username)
                .putFloat(DatabaseHelper.BALANCE_COL, balance)
                .apply();
    }
}