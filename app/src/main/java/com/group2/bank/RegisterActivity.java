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

import java.math.BigDecimal;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String TAG = RegisterActivity.class.getSimpleName();

    private TextInputLayout userLayout, passwordLayout;
    private TextInputEditText usernameEditText, passwordEditText;
    private String username, password, initialBalance = "";
    private BigDecimal initialBalanceDecimal;
    private TextInputLayout amountLayout;
    private TextInputEditText amountEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userLayout = findViewById(R.id.layout);
        passwordLayout = findViewById(R.id.layout2);
        amountLayout = findViewById(R.id.layout3);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        amountEditText = findViewById(R.id.amount);


        // switch to the MainActivity on "register" button press if the inputs are valid
        final Button register = findViewById(R.id.register_button);
        register.setOnClickListener(v -> {

            retrieveInputs();

            if (isVerified()) {

                try (DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
                     SQLiteDatabase db = dbHelper.getWritableDatabase()) {

                    if (DatabaseHelper.registerAccount(db, username, Authentication.SHA256(password), initialBalanceDecimal) == -1) {
                        throw new SQLiteException(String.format("Account not registered for username '%s', password '%s', and balance '%s'",
                                username, password, initialBalanceDecimal));
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


        // enforce decimal input filter
        amountEditText.setFilters(new InputFilter[]{new LoggedInActivity.DecimalDigitsInputFilter()});

        //clear the error if input has been edited
        //try to register when the enter key is pressed
        amountEditText.setOnEditorActionListener((v, actionId, event) -> {
            amountLayout.setError(null);

            if (actionId == EditorInfo.IME_ACTION_DONE) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(amountEditText.getWindowToken(), 0);
            }
            return false;
        });


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
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
    }

    /**
     * Retrieve the username, password, and initialBalance inputted
     */
    private void retrieveInputs() {
        username = usernameEditText.getText().toString();
        password = passwordEditText.getText().toString();
        initialBalance = amountEditText.getText().toString();
    }

    /**
     * Verifies that the username, password, and initial balance are valid
     *
     * @return true if the username and the password are valid; otherwise, false
     */
    private boolean isVerified() {
        // validate username
        boolean check = Authentication.isValid(true, username, this);
        if (!check) {
            userLayout.setError(getString(R.string.username_invalid));
        }

        // validate password
        if (!Authentication.isValid(false, password, this)) {
            Log.e(TAG, "here");
            passwordLayout.setError(getString(R.string.password_invalid));
            check = false;
        }

        // validate initial balance

        if (initialBalance.trim().equals("")) {
            amountLayout.setError(getString(R.string.amount_invalid));
            return false;
        }

        initialBalanceDecimal = new BigDecimal(initialBalance);

        // ensure the individual amount input is non-negative and < 4294967295.99 as required by spec
        if (initialBalanceDecimal.doubleValue() <= 0.00f || initialBalanceDecimal.doubleValue() > 4294967295.99f) {
            amountLayout.setError(getString(R.string.amount_invalid));
            return false;
        }

        String[] splitBalance = initialBalance.split("\\.");

        // ensure there is exactly one decimal in the amount and exactly 2 digits after the decimal
        if (!(splitBalance.length == 2 && splitBalance[1].length() == 2)) {
            amountLayout.setError(getString(R.string.amount_invalid));
            return false;
        }

        return check;
    }


}