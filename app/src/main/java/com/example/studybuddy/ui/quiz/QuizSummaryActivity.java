package com.example.studybuddy.ui.quiz;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studybuddy.R;
import com.example.studybuddy.data.model.QuizQuestion;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * Score summary shown after the last question. Offers retaking the exact
 * same question set (no new Gemini call) or returning to the chat screen.
 */
public class QuizSummaryActivity extends AppCompatActivity {

    private static final String EXTRA_SUBJECT_NAME = "subjectName";
    private static final String EXTRA_SCORE = "score";
    private static final String EXTRA_TOTAL = "total";
    private static final String EXTRA_QUESTIONS_JSON = "questionsJson";

    public static void start(Context context, String subjectName, int score, int total, List<QuizQuestion> questions) {
        String json = new Gson().toJson(questions);
        Intent intent = new Intent(context, QuizSummaryActivity.class);
        intent.putExtra(EXTRA_SUBJECT_NAME, subjectName);
        intent.putExtra(EXTRA_SCORE, score);
        intent.putExtra(EXTRA_TOTAL, total);
        intent.putExtra(EXTRA_QUESTIONS_JSON, json);
        context.startActivity(intent);
    }

    private String subjectName;
    private List<QuizQuestion> questions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_summary);

        subjectName = getIntent().getStringExtra(EXTRA_SUBJECT_NAME);
        int score = getIntent().getIntExtra(EXTRA_SCORE, 0);
        int total = getIntent().getIntExtra(EXTRA_TOTAL, 0);
        String questionsJson = getIntent().getStringExtra(EXTRA_QUESTIONS_JSON);

        questions = new Gson().fromJson(
                questionsJson,
                new TypeToken<List<QuizQuestion>>() {}.getType()
        );

        TextView subjectLabel = findViewById(R.id.summarySubjectLabel);
        TextView scoreText = findViewById(R.id.summaryScoreText);
        TextView percentText = findViewById(R.id.summaryPercentText);
        LinearLayout stripContainer = findViewById(R.id.summaryStripContainer);
        TextView retakeButton = findViewById(R.id.btnRetakeQuiz);
        TextView backButton = findViewById(R.id.btnBackToChat);

        subjectLabel.setText(subjectName);
        scoreText.setText(score + " / " + total);

        int percent = total > 0 ? Math.round((score / (float) total) * 100) : 0;
        percentText.setText(percent + "% correct");

        renderStrip(stripContainer, total);

        retakeButton.setOnClickListener(v -> {
            QuizActivity.start(this, subjectName, questions);
            finish();
        });

        backButton.setOnClickListener(v -> {
            // Pop back to ChatActivity. QuizActivity already called finish()
            // on itself before launching this screen, so a normal finish()
            // here returns directly to the chat in the activity stack.
            finish();
        });
    }

    /**
     * Renders `total` evenly-spaced bars as a generic visual recap of quiz
     * length. Not tied to per-question correctness -- this screen only
     * tracks the aggregate score, not which specific questions were missed.
     */
    private void renderStrip(LinearLayout container, int total) {
        container.removeAllViews();
        for (int i = 0; i < total; i++) {
            View bar = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, dpToPx(8), 1f
            );
            int marginPx = dpToPx(3);
            params.setMargins(marginPx, 0, marginPx, 0);
            bar.setLayoutParams(params);
            bar.setBackgroundResource(R.drawable.bg_quiz_strip_bar);
            container.addView(bar);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density);
    }
}