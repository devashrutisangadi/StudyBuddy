package com.example.studybuddy.ui.notes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.data.model.Note;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    public interface OnNoteDeleteListener {
        void onDelete(Note note);
    }

    public interface OnNoteClickListener {
        void onClick(Note note);
    }

    private List<Note> notes = new ArrayList<>();
    private final OnNoteDeleteListener deleteListener;
    private final OnNoteClickListener clickListener;

    public NoteAdapter(OnNoteDeleteListener deleteListener, OnNoteClickListener clickListener) {
        this.deleteListener = deleteListener;
        this.clickListener = clickListener;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes != null ? notes : new ArrayList<>();
        notifyDataSetChanged();
    }

    public boolean isEmpty() {
        return notes.isEmpty();
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
        Note note = notes.get(position);

        boolean isPdf = note.fileName != null && !note.fileName.trim().isEmpty();

        if (isPdf) {
            holder.tvIcon.setText("📄");
            holder.tvPreview.setText(note.fileName);
        } else {
            holder.tvIcon.setText("📝");
            String preview = note.content != null ? note.content.trim() : "";
            if (preview.length() > 60) {
                preview = preview.substring(0, 60) + "...";
            }
            holder.tvPreview.setText(preview.isEmpty() ? "(empty note)" : preview);
        }

        String dateStr = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
                .format(note.createdAt);
        holder.tvDate.setText("Added " + dateStr);

        holder.itemView.setOnClickListener(v -> clickListener.onClick(note));
        holder.tvDelete.setOnClickListener(v -> deleteListener.onDelete(note));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvPreview, tvDate, tvDelete;

        NoteViewHolder(View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvNoteIcon);
            tvPreview = itemView.findViewById(R.id.tvNotePreview);
            tvDate = itemView.findViewById(R.id.tvNoteDate);
            tvDelete = itemView.findViewById(R.id.tvDeleteNote);
        }
    }
}