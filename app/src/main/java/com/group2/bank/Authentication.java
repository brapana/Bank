package com.group2.bank;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;


/**
 * Authentication helper functions
 */
public class Authentication {

    public static MessageDigest md = null;

    /**
     * Takes in a string and returns the SHA256 hashed version
     *
     * @return SHA256 hashed version of the input string
      */
    public static String SHA256(String password) {

        // Initialize MessageDigest object to use SHA-256 encryption
        if (md == null) {
            try {
                md = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                System.err.println("SHA-256 algorithm not found on this device, unable to authenticate.");
                return null;
            }
        }

        // retrieve hashed SHA-256 representation of password
        md.update(password.getBytes());
        byte[] digest = md.digest();
        String hashedPassword = Base64.encodeToString(digest, Base64.DEFAULT);

        return hashedPassword;
    }

    /**
     * Verifies that the input satisfies the following constraints:
     *  - is not empty
     *  - contains only underscores, hyphens, dots, digits, or lowercase alphabetical characters
     *  - does not exist in the database
     *
     * @return true if the input is valid; otherwise, false
     */
    public static boolean isValid(boolean isUsername, String input, Context context) {
        if (input.trim().isEmpty()) return false;

        //constraints check
        final String regex = "[_\\-\\.0-9a-z]+";
        Pattern pattern = Pattern.compile(regex);
        if (!pattern.matcher(input).matches()) return false;

        //unique check
        if (isUsername)
            return isUniqueUsername(input, context);
        else
            return true;
    }

    /**
     * Verifies that the username does not exist in the database
     *
     * @return true if the username is unique; otherwise, false
     */
    public static boolean isUniqueUsername(String username, Context context) {
        final String query = String.format("SELECT * FROM %s WHERE %s = '%s'; ",
                DatabaseHelper.ACCOUNTS_TABLE,
                DatabaseHelper.USERNAME_COL, username);

        try (DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
             SQLiteDatabase db = dbHelper.getReadableDatabase()) {

            try (Cursor cursor = db.rawQuery(query, new String[]{})) {
                if (cursor.getCount() > 0) {
                    return false;
                }
            }
            catch (SQLiteException e) {
                return false;
            }
        }
        catch (SQLiteException e) {
            return false;
        }

        return true;
    }
}
