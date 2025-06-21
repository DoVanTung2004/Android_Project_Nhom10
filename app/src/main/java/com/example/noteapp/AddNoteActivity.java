package com.example.noteapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.noteapp.models.Note;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class AddNoteActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;

    private EditText etTitle, etContent;
    private Button btnSave;
    private ImageButton btnAttach;
    private ImageView imagePreview;

    private Uri imageUri;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        btnSave = findViewById(R.id.btnSave);
        btnAttach = findViewById(R.id.btnAttach);
        imagePreview = findViewById(R.id.imagePreview);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        btnAttach.setOnClickListener(v -> showImagePickerMenu());

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }

            if (imageUri != null) {
                uploadImageToFirebase(title, content, imageUri);
            } else {
                saveNoteToFirestore(new Note(title, content));
            }
        });
    }

    private void showImagePickerMenu() {
        PopupMenu popup = new PopupMenu(this, btnAttach);
        popup.getMenu().add("Chọn ảnh từ thư viện");
        popup.getMenu().add("Chụp ảnh");

        popup.setOnMenuItemClickListener(item -> {
            String choice = item.getTitle().toString();
            if (choice.contains("Chọn")) {
                Intent pickIntent = new Intent(Intent.ACTION_PICK);
                pickIntent.setType("image/*");
                startActivityForResult(pickIntent, REQUEST_IMAGE_PICK);
            } else {
                File imageFile = createImageFile();
                imageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", imageFile);

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            }
            return true;
        });
        popup.show();
    }

    private File createImageFile() {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String filename = "IMG_" + System.currentTimeMillis() + ".jpg";
        return new File(storageDir, filename);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                imageUri = data.getData();
                imagePreview.setImageURI(imageUri);
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                imagePreview.setImageURI(imageUri);
            }
        }
    }

    private void uploadImageToFirebase(String title, String content, Uri uri) {
        String filename = "notes/" + System.currentTimeMillis() + ".jpg";
        StorageReference ref = storage.getReference().child(filename);

        ref.putFile(uri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> {
                    Note note = new Note(title, content, downloadUri.toString());
                    saveNoteToFirestore(note);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void saveNoteToFirestore(Note note) {
        db.collection("notes")
                .add(note)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "Đã lưu ghi chú", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
