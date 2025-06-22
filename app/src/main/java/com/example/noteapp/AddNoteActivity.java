package com.example.noteapp;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.noteapp.models.Note;
import com.example.noteapp.models.ReminderReceiver;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class AddNoteActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private Spinner spinnerLabel, spinnerColor;
    private Button btnSave, btnReminder;
    private CheckBox checkboxPrivate;
    private long selectedReminderTime = 0;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        // Ánh xạ view
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        spinnerLabel = findViewById(R.id.spinnerLabel);
        spinnerColor = findViewById(R.id.spinnerColor);
        btnSave = findViewById(R.id.btnSave);
        btnReminder = findViewById(R.id.btnReminder);
        checkboxPrivate = findViewById(R.id.checkboxPrivate);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Kiểm tra người dùng đã đăng nhập chưa
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_notes) {
                startActivity(new Intent(this, HomeActivity.class));
                return true;
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });

        // Xử lý click
        btnReminder.setOnClickListener(v -> showDateTimePicker());
        btnSave.setOnClickListener(v -> saveNote());
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

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề và nội dung", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gán mã màu theo tên
        String colorHex = "#FFFFFF"; // mặc định trắng
        switch (colorName) {
            case "Hồng": colorHex = "#F8BBD0"; break;
            case "Vàng": colorHex = "#FFF59D"; break;
            case "Xanh": colorHex = "#B2EBF2"; break;
        }

        boolean isPrivate = checkboxPrivate.isChecked();
        String userId = auth.getCurrentUser().getUid();
        String noteId = db.collection("notes").document().getId();

        Note note = new Note(title, content, label, colorHex, selectedReminderTime, isPrivate);
        note.setId(noteId);
        note.setUserId(userId);

        db.collection("notes")
                .document(noteId)
                .set(note)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Ghi chú đã lưu", Toast.LENGTH_SHORT).show();
                    if (selectedReminderTime > 0) {
                        scheduleReminder(note, selectedReminderTime);
                    }
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }
    }
}
