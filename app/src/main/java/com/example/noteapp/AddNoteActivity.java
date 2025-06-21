package com.example.noteapp;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.noteapp.models.Note;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import com.example.noteapp.models.ReminderReceiver;


public class AddNoteActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private Spinner spinnerLabel, spinnerColor;
    private Button btnSave, btnReminder;
    private long selectedReminderTime = 0;
    private CheckBox checkboxPrivate;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        // Ánh xạ các view
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        spinnerLabel = findViewById(R.id.spinnerLabel);
        spinnerColor = findViewById(R.id.spinnerColor);
        btnSave = findViewById(R.id.btnSave);
        btnReminder = findViewById(R.id.btnReminder);

        db = FirebaseFirestore.getInstance();

        btnReminder.setOnClickListener(v -> showDateTimePicker());
        btnSave.setOnClickListener(v -> saveNote());
        checkboxPrivate = findViewById(R.id.checkboxPrivate);

    }

    private void showDateTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            new TimePickerDialog(this, (view1, hour, minute) -> {
                calendar.set(year, month, day, hour, minute, 0);
                selectedReminderTime = calendar.getTimeInMillis();
                Toast.makeText(this, "Đã chọn giờ nhắc: " + hour + ":" + String.format("%02d", minute), Toast.LENGTH_SHORT).show();
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveNote() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        String label = spinnerLabel.getSelectedItem().toString();
        String colorName = spinnerColor.getSelectedItem().toString();

        // Gán mã màu dựa vào tên màu
        String colorHex = "#FFFFFF"; // mặc định trắng
        switch (colorName) {
            case "Hồng": colorHex = "#F8BBD0"; break;
            case "Vàng": colorHex = "#FFF59D"; break;
            case "Xanh": colorHex = "#B2EBF2"; break;
        }

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề và nội dung", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean isPrivate = checkboxPrivate.isChecked();
        // Tạo object Note và lưu vào Firestore
        Note note = new Note(title, content, label, colorHex, selectedReminderTime, isPrivate);
        db.collection("notes")
                .add(note)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Ghi chú đã lưu", Toast.LENGTH_SHORT).show();
                    if (selectedReminderTime > 0) {
                        scheduleReminder(note, selectedReminderTime);
                    }
                    finish();
                });

    }

    private void scheduleReminder(Note note, long timeInMillis) {
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("title", note.getTitle());
        intent.putExtra("content", note.getContent());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
        }
    }
}