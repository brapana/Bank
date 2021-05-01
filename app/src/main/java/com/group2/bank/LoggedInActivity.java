package com.group2.bank;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import org.w3c.dom.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoggedInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_session_info), MODE_PRIVATE);
        String username = "Hello, "+sharedPref.getString(DatabaseHelper.USERNAME_COL,"");
        TextView user = findViewById(R.id.title);
        user.setText(username);

        TextView balance = findViewById(R.id.balance);
        float money = sharedPref.getFloat(DatabaseHelper.BALANCE_COL, 0);
        String money_bal = "Balance: $"+money;
        balance.setText(money_bal);

        final ImageButton logout = findViewById(R.id.logout);
        logout.setOnClickListener(v -> startActivity(new Intent(LoggedInActivity.this, MainActivity.class)));

        final TextInputEditText amount = findViewById(R.id.amount);
        amount.setFilters(new InputFilter[]{new DecimalDigitsInputFilter()});




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

    public void UpdateBalance(){

    }
}