package com.example.noteapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noteapp.models.Note;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements NoteAdapter.OnNoteActionListener {

    private static final int REQUEST_CODE_ADD_NOTE = 1;

    private Button btnAdd;
    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private ArrayList<Note> noteList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Nhận email từ màn hình trước
        String email = getIntent().getStringExtra("email");
        if (email != null && !email.isEmpty()) {
            Toast.makeText(this, "Chào mừng " + email, Toast.LENGTH_LONG).show();
        }

        btnAdd = findViewById(R.id.btn_add);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        adapter = new NoteAdapter(noteList, this); // Truyền listener là Activity này
        recyclerView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddNoteActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
        });

        loadNotes();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            loadNotes();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    private void loadNotes() {
        db.collection("notes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    noteList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Note note = doc.toObject(Note.class);
                        note.setId(doc.getId()); // Gán document ID cho Note
                        noteList.add(note);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải ghi chú: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Xử lý khi người dùng chọn xóa từ adapter
    @Override
    public void onDelete(Note note) {
        db.collection("notes")
                .document(note.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    noteList.remove(note);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Đã xóa ghi chú", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Xử lý khi người dùng chọn ghim
    @Override
    public void onPin(Note note) {
        noteList.remove(note);
        noteList.add(0, note);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Đã ghim ghi chú", Toast.LENGTH_SHORT).show();
    }

    // Xử lý cập nhật (nếu có màn hình sửa thì mở ở đây)
    @Override
    public void onUpdate(Note note) {
        Intent intent = new Intent(HomeActivity.this, EditNoteActivity.class);
        intent.putExtra("note_id", note.getId());
        intent.putExtra("title", note.getTitle());
        intent.putExtra("content", note.getContent());
        startActivity(intent);
    }
}
