package com.example.studybuddy.ui.quiz.history;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.data.model.QuizAttempt;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QuizAttemptAdapter extends RecyclerView.Adapter<QuizAttemptAdapter.ViewHolder> {

    private List<QuizAttempt> attempts = new ArrayList<>();

    public void setAttempts(List<QuizAttempt> attempts) {
        this.attempts = attempts != null ? attempts : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quiz_attempt, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(attempts.get(position));
    }

    @Override
    public int getItemCount() {
        return attempts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvDate;
        private final TextView tvScore;
        private final LinearLayout dotStrip;

        ViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvAttemptDate);
            tvScore = itemView.findViewById(R.id.tvAttemptScore);
            dotStrip = itemView.findViewById(R.id.dotStripContainer);
        }

        void bind(QuizAttempt attempt) {
            // Format date as "Jun 29, 2026 at 9:47 PM"
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                    "MMM d, yyyy 'at' h:mm a", java.util.Locale.getDefault());
            tvDate.setText(sdf.format(new Date(attempt.timestamp)));

            tvScore.setText(attempt.score + " / " + attempt.total);

            // Parse missed indices
            Set<Integer> missedSet = new HashSet<>();
            if (attempt.missedIndices != null && !attempt.missedIndices.isEmpty()) {
                for (String part : attempt.missedIndices.split(",")) {
                    try { missedSet.add(Integer.parseInt(part.trim())); } catch (NumberFormatException ignored) {}
                }
            }

            // Build per-question dot strip
            Context ctx = itemView.getContext();
            dotStrip.removeAllViews();
            int dotSizePx = (int) (8 * ctx.getResources().getDisplayMetrics().density);
            int marginPx = (int) (3 * ctx.getResources().getDisplayMetrics().density);

            for (int i = 0; i < attempt.total; i++) {
                View dot = new View(ctx);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dotSizePx, dotSizePx);
                params.setMargins(0, 0, marginPx, 0);
                dot.setLayoutParams(params);
                dot.setBackgroundResource(missedSet.contains(i)
                        ? R.drawable.bg_dot_incorrect
                        : R.drawable.bg_dot_correct);
                dotStrip.addView(dot);
            }
        }
    }
}