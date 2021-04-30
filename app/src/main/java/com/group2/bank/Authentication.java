package com.group2.bank;
import android.util.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Authentication helper functions
 */
public class Authentication {

    public static MessageDigest md = null;

    // takes in a string and returns the SHA256 hashed version
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
}
