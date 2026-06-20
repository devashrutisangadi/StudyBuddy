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

        SubjectViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvSubjectName);
            tvDate = itemView.findViewById(R.id.tvSubjectDate);
        }
    }
}