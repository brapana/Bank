package com.group2.bank;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.ImageButton;

import com.google.android.material.textfield.TextInputEditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoggedInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        final ImageButton logout = findViewById(R.id.logout);
        logout.setOnClickListener(v -> startActivity(new Intent(LoggedInActivity.this, MainActivity.class)));

        final TextInputEditText amount = findViewById(R.id.amount);
        amount.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(10, 2)});
    }

    /**
     * FIX REGEX:
     *  - Restricts the decimals before digit to be up to 10, but if a period isn't typed then user can go up to the 11th place
     *  - Doesn't check for leading 0's for example: 0123.12
     *
     * Source: This class is taken from Azhar on Tutorialspoint.
     *    URL: https://www.tutorialspoint.com/how-to-limit-decimal-places-in-android-edittext
     */
    class DecimalDigitsInputFilter implements InputFilter {
        private Pattern mPattern;
        DecimalDigitsInputFilter(int digitsBeforeZero, int digitsAfterZero) {
            mPattern = Pattern.compile("[0-9]{0," + digitsBeforeZero + "}+((\\.[0-9]{0," + (digitsAfterZero - 1) + "})?)||(\\.)?");
        }
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Matcher matcher = mPattern.matcher(dest);
            if (!matcher.matches())
                return "";
            return null;
        }
    }
}