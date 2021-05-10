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

import java.math.BigDecimal;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoggedInActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String BALANCE_DISPLAY = "Balance: $%.2f";

    private TextInputLayout amountLayout;
    private TextInputEditText amountEditText;
    private String username;
    private BigDecimal accountBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_session_info), MODE_PRIVATE);
        username = sharedPref.getString(DatabaseHelper.USERNAME_COL, "");

        // retrieve account balance from sharedprefs and convert from integer representation to BigDecimal
        accountBalance = new BigDecimal(sharedPref.getInt(DatabaseHelper.BALANCE_COL, 0));
        accountBalance = accountBalance.divide(new BigDecimal(100));

        final TextView balanceDisplay = findViewById(R.id.balance);
        balanceDisplay.setText(String.format(BALANCE_DISPLAY, accountBalance));

        final Button withdraw = findViewById(R.id.withdraw_button);
        withdraw.setOnClickListener(v ->
                balanceDisplay.setText(
                        String.format(BALANCE_DISPLAY, updateBalance(true))));

        final Button deposit = findViewById(R.id.deposit_button);
        deposit.setOnClickListener(v ->
                balanceDisplay.setText(
                        String.format(BALANCE_DISPLAY, updateBalance(false))));

        final TextView user = findViewById(R.id.title);
        user.setText("Hello, " + username);

        final ImageButton logout = findViewById(R.id.logout);
        logout.setOnClickListener(v -> {
            sharedPref.edit().clear().commit();
            startActivity(new Intent(LoggedInActivity.this, MainActivity.class));
        });

        amountLayout = findViewById(R.id.layout);
        amountEditText = findViewById(R.id.amount);

        // enforce decimal input filter
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
    }

    @Override
    public void onBackPressed() {
        findViewById(R.id.logout).callOnClick();
    }

    /**
     *  Restricts input to decimal numbers (max of 2 decimal digits) without leading zeros
     */
    static class DecimalDigitsInputFilter implements InputFilter {
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
     * Get the amount entered
     *
     * @return a String representation of the input
     */
    private String getInput() {
        return Objects.requireNonNull(amountEditText.getText()).toString();
    }

    /**
     * Verifies whether the inputted value is valid for withdraw or deposit
     *
     * @param amountInputString a String representation of the input
     * @param amountInput the value inputted
     * @param isWithdraw true if the amount is to be withdrawn from the account; false if the amount is to be deposited
     * @param newBalance the value to replace the current balance by
     * @return true if the inputted value is valid for withdraw or deposit; otherwise, false
     */
    private boolean isVerified(String amountInputString, BigDecimal amountInput, boolean isWithdraw, BigDecimal newBalance) {

        // ensure the individual amount input is non-negative and < 4294967295.99 as required by spec
        if (amountInput.doubleValue() <= 0.00f || amountInput.doubleValue() > 4294967295.99f) {
            System.out.println("why?");
            return false;
        }

        String[] splitBalance = amountInputString.split("\\.");

        // ensure there is exactly one decimal in the amount and exactly 2 digits after the decimal
        if (!(splitBalance.length == 2 && splitBalance[1].length() == 2)) {
            return false;
        }

        // if withdrawing, ensure the resulting balance is non-negative
        if (isWithdraw && newBalance.doubleValue() < 0.00) {
            return false;
        }

        return true;
    }

    /**
     * Updates the account balance by subtracting or adding the current balance by the amount inputted
     *
     * @param isWithdraw true if the amount is to be withdrawn from the account; false if the amount is to be deposited
     * @return the latest balance value in the db
     */
    private BigDecimal updateBalance(boolean isWithdraw) {
        String amountInputString = getInput();

        final BigDecimal amountInput = (!amountInputString.trim().equals("")) ? new BigDecimal(amountInputString) : new BigDecimal(0.00);

        final BigDecimal newBalance = (isWithdraw) ? accountBalance.subtract(amountInput) : accountBalance.add(amountInput);

        if (isVerified(amountInputString, amountInput, isWithdraw, newBalance)) {

            try (DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
                 SQLiteDatabase db = dbHelper.getWritableDatabase()) {

                if (DatabaseHelper.updateBalance(db, username, newBalance) <= 0) {

                    amountLayout.setError(getString(R.string.unexpected_error));

                    throw new SQLiteException(
                            String.format("Failed to update balance; entry not found for the username: %s", username));
                }

                accountBalance = newBalance;
            }
            catch (SQLiteException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        else {
            amountLayout.setError(getString(R.string.amount_invalid));
        }

        return accountBalance;
    }
}