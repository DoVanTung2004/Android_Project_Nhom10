package com.example.noteapp;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.noteapp.models.Note;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> noteList;
    private OnNoteActionListener listener;
    private FirebaseFirestore db;

    public interface OnNoteActionListener {
        void onDelete(Note note);
        void onUpdate(Note note);
        void onPin(Note note);
    }

    public NoteAdapter(List<Note> noteList, OnNoteActionListener listener) {
        this.noteList = noteList;
        this.listener = listener;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);
        holder.tvTitle.setText(note.getTitle());
        holder.tvContent.setText(note.getContent());
        holder.checkBoxComplete.setOnCheckedChangeListener(null);
        holder.checkBoxComplete.setChecked(note.isCompleted());

        // Gạch ngang nếu hoàn thành
        if (note.isCompleted()) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        // Load ảnh nếu có
        if (note.getImageUrl() != null && !note.getImageUrl().isEmpty()) {
            holder.imageThumbnail.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(note.getImageUrl())
                    .into(holder.imageThumbnail);
        } else {
            holder.imageThumbnail.setVisibility(View.GONE);
        }

        // CheckBox "Hoàn thành"
        holder.checkBoxComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            note.setCompleted(isChecked);
            db.collection("notes")
                    .document(note.getId())
                    .update("completed", isChecked)
                    .addOnSuccessListener(aVoid -> notifyItemChanged(holder.getAdapterPosition()));
        });

        // Nút 3 chấm (menu)
        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), holder.btnMore);
            popupMenu.inflate(R.menu.menu_note_options);
            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_update) {
                    if (listener != null) listener.onUpdate(note);
                    return true;
                } else if (id == R.id.menu_delete) {
                    deleteNoteAtPosition(holder.getAdapterPosition());
                    return true;
                } else if (id == R.id.menu_pin) {
                    pinNoteAtPosition(holder.getAdapterPosition());
                    return true;
                } else {
                    return false;
                }
            });
            popupMenu.show();
        });

        // Nút chia sẻ
        holder.btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);

            String shareText = "📌 " + note.getTitle() + "\n" + note.getContent();
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

            if (note.getImageUrl() != null && !note.getImageUrl().isEmpty()) {
                shareIntent.setType("text/plain"); // vì URL là dạng chuỗi
                shareText += "\n🖼️ " + note.getImageUrl();
            } else {
                shareIntent.setType("text/plain");
            }

            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            v.getContext().startActivity(Intent.createChooser(shareIntent, "Chia sẻ ghi chú"));
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    private void deleteNoteAtPosition(int position) {
        Note noteToDelete = noteList.get(position);
        db.collection("notes")
                .document(noteToDelete.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    noteList.remove(position);
                    notifyItemRemoved(position);
                    if (listener != null) {
                        listener.onDelete(noteToDelete);
                    }
                });
    }

    private void pinNoteAtPosition(int position) {
        if (position < 0 || position >= noteList.size()) return;
        Note noteToPin = noteList.remove(position);
        noteList.add(0, noteToPin);
        notifyItemMoved(position, 0);
        if (listener != null) {
            listener.onPin(noteToPin);
        }
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent;
        ImageButton btnMore, btnShare;
        CheckBox checkBoxComplete;
        ImageView imageThumbnail;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            btnMore = itemView.findViewById(R.id.btnMore);
            btnShare = itemView.findViewById(R.id.btnShare);
            checkBoxComplete = itemView.findViewById(R.id.checkBoxComplete);
            imageThumbnail = itemView.findViewById(R.id.imageThumbnail);
        }
    }
}
