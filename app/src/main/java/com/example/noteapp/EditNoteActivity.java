package com.example.noteapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditNoteActivity extends AppCompatActivity {

    private EditText editTitle, editContent;
    private CheckBox checkDone;
    private Button btnUpdate, btnCancel;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String noteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        editTitle = findViewById(R.id.editTitle);
        editContent = findViewById(R.id.editContent);
        checkDone = findViewById(R.id.checkDone);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnCancel = findViewById(R.id.btnCancel);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Nhận dữ liệu từ Intent
        noteId = getIntent().getStringExtra("note_id");
        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");
        boolean done = getIntent().getBooleanExtra("done", false);

        // Gán dữ liệu vào các trường
        editTitle.setText(title);
        editContent.setText(content);
        checkDone.setChecked(done);

        // Xử lý nút Cập nhật
        btnUpdate.setOnClickListener(v -> updateNote());

        // Xử lý nút Hủy
        btnCancel.setOnClickListener(v -> finish());
    }

    private void updateNote() {
        String title = editTitle.getText().toString().trim();
        String content = editContent.getText().toString().trim();
        boolean isDone = checkDone.isChecked();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề và nội dung", Toast.LENGTH_SHORT).show();
            return;
        }

        if (noteId == null || noteId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ID ghi chú để cập nhật", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference noteRef = db.collection("notes").document(noteId);
        noteRef.update("title", title, "content", content, "done", isDone)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditNoteActivity.this, "Đã cập nhật ghi chú", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditNoteActivity.this, "Lỗi khi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
