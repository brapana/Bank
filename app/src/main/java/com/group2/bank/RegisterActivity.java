package com.group2.bank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String TAG = RegisterActivity.class.getSimpleName();

    private TextInputLayout userLayout, passwordLayout;
    private TextInputEditText usernameEditText, passwordEditText;
    private String username, password = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userLayout = findViewById(R.id.layout);
        passwordLayout = findViewById(R.id.layout2);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);

        // switch to the MainActivity on "register" button press if the inputs are valid
        final Button register = findViewById(R.id.register_button);
        register.setOnClickListener(v -> {

            retrieveInputs();

            if (isVerified()) {

                try (DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
                     SQLiteDatabase db = dbHelper.getWritableDatabase()) {

                    if (DatabaseHelper.registerAccount(db, username, password, 0) == -1) {
                        throw new SQLiteException(String.format("Account not registered for username '%s', password '%s', and balance '%s'",
                                username, password, 0));
                    }
                }

                Intent outgoingIntent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(outgoingIntent);
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
        //try to register when the enter key is pressed
        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            passwordLayout.setError(null);

            if (actionId == EditorInfo.IME_ACTION_DONE) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);
                register.callOnClick();
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

        final Button cancel = findViewById(R.id.cancel_button);
        cancel.setOnClickListener(v -> onBackPressed());

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
     * Verifies that both the username and the password are valid
     *
     * *not tested yet
     *
     * @return true if the username and the password are valid; otherwise, false
     */
    private boolean isVerified() {
        boolean check = isValid(true);
        if (!check) userLayout.setError(getString(R.string.username_invalid));

        if (!isValid(false)) {
            Log.e(TAG, "here");
            passwordLayout.setError(getString(R.string.password_invalid));
            check = false;
        }

        return check;
    }

    /**
     * Verifies that the input satisfies the following constraints:
     *  - is not empty
     *  - contains only underscores, hyphens, dots, digits, or lowercase alphabetical characters
     *  - does not exist in the database
     *
     * @return true if the input is valid; otherwise, false
     */
    private boolean isValid(boolean isUsername) {
        String input = (isUsername) ? username : password;
        if (input.trim().isEmpty()) return false;

        //constraints check
        final String regex = "[_\\-\\.0-9a-z]+";
        Pattern pattern = Pattern.compile(regex);
        if (!pattern.matcher(input).matches()) return false;

        //unique check
        if (isUsername) return isUniqueUsername();
        else return true;
    }

    /**
     * Verifies that the username does not exist in the database
     *
     * @return true if the username is unique; otherwise, false
     */
    private boolean isUniqueUsername() {
        final String query = String.format("SELECT * FROM %s WHERE %s = %s; ",
                DatabaseHelper.ACCOUNTS_TABLE,
                DatabaseHelper.USERNAME_COL, username);

        try (DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
             SQLiteDatabase db = dbHelper.getReadableDatabase()) {

            try (Cursor cursor = db.rawQuery(query, new String[]{})) {
            }
            catch (SQLiteException e) {
                return true;
            }
        }
        catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
        }

        return false;
    }
}