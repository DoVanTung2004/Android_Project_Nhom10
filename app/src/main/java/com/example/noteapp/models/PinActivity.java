package com.example.noteapp.models;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.noteapp.MainActivity;
import com.example.noteapp.R;

public class PinActivity extends AppCompatActivity {

    private EditText etPin;
    private Button btnUnlock;
    private static final String CORRECT_PIN = "1234"; // Bạn có thể lưu trong SharedPreferences nếu muốn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        etPin = findViewById(R.id.etPin);
        btnUnlock = findViewById(R.id.btnUnlock);

        btnUnlock.setOnClickListener(v -> {
            String enteredPin = etPin.getText().toString().trim();
            if (enteredPin.equals(CORRECT_PIN)) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Sai mã PIN!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}