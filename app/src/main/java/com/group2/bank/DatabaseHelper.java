package com.group2.bank;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// Defines the bank database and the ACCOUNTS table which holds the username, password, and balances
// of each registered user
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "bank"; // the name of our database
    private static final int DB_VERSION = 1; // the version of the database

    DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
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

        // stores bank accounts including a username, a password, and a balance
        db.execSQL("CREATE TABLE ACCOUNTS (username TEXT PRIMARY KEY, "
                + "password TEXT, "
                + "balance REAL);");
    }


    // add a new user account
    private static void insertTodoList(SQLiteDatabase db, String username, String password, long initialBalance) {
        ContentValues accountValues = new ContentValues();
        accountValues.put("username", username);
        accountValues.put("password", password);
        accountValues.put("balance", initialBalance);
        db.insert("ACCOUNTS", null, accountValues);
    }

}
