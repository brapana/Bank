package com.group2.bank;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoggedInActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private TextInputLayout amountLayout;
    private TextInputEditText amountEditText;
    private String username;
    private float accountBalance, amountInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_session_info), MODE_PRIVATE);
        username = sharedPref.getString(DatabaseHelper.USERNAME_COL, "");
        accountBalance = sharedPref.getFloat(DatabaseHelper.BALANCE_COL, 0);

        final TextView user = findViewById(R.id.title);
        user.setText("Hello, " + username);

        final TextView balance = findViewById(R.id.balance);
        balance.setText("Balance: $" + accountBalance);

        final ImageButton logout = findViewById(R.id.logout);
        logout.setOnClickListener(v -> startActivity(new Intent(LoggedInActivity.this, MainActivity.class)));

        amountLayout = findViewById(R.id.layout);
        amountEditText = findViewById(R.id.amount);
        amountEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter()});

        // clear error message when the input is edited
        // hide soft keyboard on enter key down
        amountEditText.setOnEditorActionListener((v, actionId, event) -> {
            amountLayout.setError(null);

            if (actionId == EditorInfo.IME_ACTION_DONE) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(amountEditText.getWindowToken(), 0);
            }
            return false;
        });

        final Button withdraw = findViewById(R.id.withdraw_button);
        withdraw.setOnClickListener(v -> updateBalance(true));

        final Button deposit = findViewById(R.id.deposit_button);
        deposit.setOnClickListener(v -> updateBalance(false));
    }

    /**
     *  Restricts input to decimal numbers (max of 2 decimal digits) without leading zeros
     */
    class DecimalDigitsInputFilter implements InputFilter {
        private Pattern mPattern;
        DecimalDigitsInputFilter() {
            mPattern = Pattern.compile("^(0|[1-9][0-9]*)(\\.[0-9]{0,2})?$");
        }
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            String result = dest.subSequence(0, dstart) + source.toString()
                    + dest.subSequence(dend, dest.length());

            Matcher matcher = mPattern.matcher(result);

            if (!matcher.matches())
                return "";
            return null;
        }
    }

    /**
     * Retrieve the amount entered
     */
    private void retrieveInput() {
        try {
            amountInput = Float.parseFloat(
                    Objects.requireNonNull(amountEditText.getText()).toString());
        }
        catch (NumberFormatException e) {
            amountInput = 0;
        }
    }

    // logic check
    private boolean isVerified(boolean isWithdraw) {
        retrieveInput();

        return false;
    }

    /**
     * Updates the account balance by subtracting or adding the current balance by the amount inputted
     *
     * @param isWithdraw true if the amount is to be withdrawn from the account; false if the amount is to be deposited
     */
    private void updateBalance(boolean isWithdraw) {
        if (isVerified(isWithdraw)) {

            float newBalance = (isWithdraw) ? accountBalance - amountInput : accountBalance + amountInput;

            try (DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
                 SQLiteDatabase db = dbHelper.getWritableDatabase()) {

                if (DatabaseHelper.updateBalance(db, username, newBalance) == 0) {
                    throw new SQLiteException(String.format("Entry not found for the username: %s", username));
                }
            }
            catch (SQLiteException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        else {
            amountLayout.setError(getString(R.string.amount_invalid));
        }
    }
}