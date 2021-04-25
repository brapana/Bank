package com.group2.bank;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // switch to the MainActivity on "register" button press
        final Button register = findViewById(R.id.register_button);
        register.setOnClickListener(v -> {

            Intent outgoingIntent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(outgoingIntent);

        });

        final Button cancel = findViewById(R.id.cancel_button);
        cancel.setOnClickListener(v -> onBackPressed());
    }
}