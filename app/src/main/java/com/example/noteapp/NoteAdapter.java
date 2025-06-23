package com.example.noteapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noteapp.models.Note;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private final List<Note> noteList;
    private final OnNoteActionListener listener;
    private final FirebaseFirestore db;

    public interface OnNoteActionListener {
        void onDelete(Note note);
        void onUpdate(Note note);
        void onPin(Note note);
    }

    public NoteAdapter(List<Note> noteList, OnNoteActionListener listener) {
        this.noteList = noteList;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
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
        holder.tvLabel.setText(note.getLabel());
        holder.checkboxDone.setChecked(note.isDone());

        applyStrikeThrough(holder.tvTitle, note.isDone());

        try {
            holder.itemView.setBackgroundColor(Color.parseColor(note.getColor()));
        } catch (Exception e) {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }

        holder.checkboxDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            note.setDone(isChecked);
            applyStrikeThrough(holder.tvTitle, isChecked);
            db.collection("notes")
                    .document(note.getId())
                    .update("done", isChecked);
        });

        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), holder.btnMore);
            popup.inflate(R.menu.menu_note_options);
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_update && listener != null) {
                    listener.onUpdate(note);
                    return true;
                } else if (id == R.id.menu_delete) {
                    deleteNote(position);
                    return true;
                } else if (id == R.id.menu_pin) {
                    pinNote(position);
                    return true;
                }
                else if (id == R.id.menu_share) {
                shareNote(holder.itemView.getContext(), note);
                return true;
                }
            return false;
            });
            popup.show();
        });
    }

    private void applyStrikeThrough(TextView textView, boolean done) {
        if (done) {
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            textView.setPaintFlags(textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    private void deleteNote(int position) {
        if (position < 0 || position >= noteList.size()) return;

        Note note = noteList.get(position);
        db.collection("notes").document(note.getId()).delete()
                .addOnSuccessListener(unused -> {
                    noteList.remove(position);
                    notifyItemRemoved(position);
                    if (listener != null) listener.onDelete(note);
                });
    }

    private void pinNote(int position) {
        if (position < 0 || position >= noteList.size()) return;

        Note note = noteList.remove(position);
        noteList.add(0, note);
        notifyItemMoved(position, 0);
        if (listener != null) listener.onPin(note);
    }
    private void shareNote(Context context, Note note) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        String shareContent = "📝 " + note.getTitle() + "\n\n" + note.getContent();
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);

        context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ ghi chú qua..."));
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvLabel;
        CheckBox checkboxDone;
        ImageButton btnMore;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            checkboxDone = itemView.findViewById(R.id.checkboxDone);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
