package com.group2.bank;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // switch to RegisterActivity on "register" button press
        final Button registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(v -> {

            Intent outgoingIntent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(outgoingIntent);

        });

        // switch to LoggedInActivity on "login" button press
        final Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(v -> {

            Intent outgoingIntent = new Intent(MainActivity.this, LoggedInActivity.class);
            startActivity(outgoingIntent);

        });
    }
}