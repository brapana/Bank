package com.group2.bank;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// Defines the bank database and the ACCOUNTS table which holds the username, password, and balances
// of each registered user
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "bank"; // the name of our database
    private static final int DB_VERSION = 3; // the version of the database

    public static final String ACCOUNTS_TABLE = "ACCOUNTS";
    public static final String USERNAME_COL = "username";
    public static final String PASSWORD_COL = "password";
    public static final String BALANCE_COL = "balance";

    private static DatabaseHelper instance;

    DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) instance = new DatabaseHelper(context.getApplicationContext());
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        updateMyDatabase(db, 0, DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateMyDatabase(db, oldVersion, newVersion);
    }

    private void updateMyDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {

        // when version updates, drop previously made tables
        db.execSQL("DROP TABLE IF EXISTS ACCOUNTS;");

        // stores bank accounts including a username, a password, and a balance (balance stored as
        // an integer, displayed to user in decimal form shifted left two digits)
        db.execSQL("CREATE TABLE ACCOUNTS (username TEXT PRIMARY KEY, "
                + "password TEXT, "
                + "balance INTEGER);");
    }

    /**
     * Adds a new account entry with the argument values
     *
     * @param db
     * @param username
     * @param password
     * @param initialBalance
     * @return the row id of the newly inserted entry, or -1 if an error occurred
     */
    public static long registerAccount(SQLiteDatabase db, String username, String password, BigDecimal initialBalance) {
        ContentValues accountValues = new ContentValues();
        accountValues.put(USERNAME_COL, username);
        accountValues.put(PASSWORD_COL, password);
        accountValues.put(BALANCE_COL, initialBalance.multiply(new BigDecimal(100)).intValue());
        return db.insert(ACCOUNTS_TABLE, null, accountValues);
    }


    /**
     * Updates the balance value
     *
     * @param db
     * @param username the username of the account entry to change
     * @param newBalance the new balance value to replace the current balance value by
     * @return the number of rows affected
     */
    public static int updateBalance(SQLiteDatabase db, String username, BigDecimal newBalance) {
        ContentValues cv = new ContentValues();
        cv.put(BALANCE_COL, newBalance.multiply(new BigDecimal(100)).intValue());
        return db.update(ACCOUNTS_TABLE, cv, USERNAME_COL + " = ?", new String[] {username});
    }
}
