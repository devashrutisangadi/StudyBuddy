package com.example.studybuddy.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.data.model.Subject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    public interface OnSubjectClickListener {
        void onClick(Subject subject);
        void onLongClick(Subject subject);
    }

    // Rotating folder colors — cycles per card position
    private static final int[] FOLDER_COLORS = {
            0xFFFFD3D3, // coral
            0xFFD4F0D0, // sage green
            0xFFFFF3B8, // warm yellow
            0xFFC9E4FF, // sky blue
            0xFFE3D5F5, // lavender
            0xFFFFE0C2  // peach
    };

    private List<Subject> subjects = new ArrayList<>();
    private final OnSubjectClickListener listener;

    public SubjectAdapter(OnSubjectClickListener listener) {
        this.listener = listener;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        Subject subject = subjects.get(position);
        holder.tvName.setText(subject.name);

        String dateStr = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(subject.createdAt);
        holder.tvDate.setText("Created on " + dateStr);

        int color = FOLDER_COLORS[position % FOLDER_COLORS.length];

        if (holder.viewFolderShape.getBackground() instanceof FolderShapeDrawable) {
            ((FolderShapeDrawable) holder.viewFolderShape.getBackground()).setFillColor(color);
        } else {
            holder.viewFolderShape.setBackground(new FolderShapeDrawable(color));
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(subject));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(subject);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    static class SubjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate;
        View viewFolderShape;

        SubjectViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvSubjectName);
            tvDate = itemView.findViewById(R.id.tvSubjectDate);
            viewFolderShape = itemView.findViewById(R.id.viewFolderShape);
        }
    }
}