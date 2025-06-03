package com.example.noteapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noteapp.models.Note;
import com.example.noteapp.R;
import com.google.firebase.firestore.FirebaseFirestore;

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

    private void deleteNoteAtPosition(int position) {
        Note noteToDelete = noteList.get(position);

        // Xóa trên Firestore
        db.collection("notes")
                .document(noteToDelete.getId())  // giả sử Note có method getId() trả về documentId
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Xóa khỏi danh sách và cập nhật adapter
                    noteList.remove(position);
                    notifyItemRemoved(position);

                    if (listener != null) {
                        listener.onDelete(noteToDelete);
                    }
                })
                .addOnFailureListener(e -> {
                    // Xử lý lỗi, có thể thông báo lỗi cho người dùng
                    // Ví dụ: Toast hoặc log
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
    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);
        holder.tvTitle.setText(note.getTitle());
        holder.tvContent.setText(note.getContent());
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
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent;
        ImageButton btnMore;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }

}
