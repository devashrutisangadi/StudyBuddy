package com.example.studybuddy.ui.quiz;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.studybuddy.R;
import com.example.studybuddy.data.model.QuizQuestion;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * Interactive one-question-at-a-time quiz screen.
 *
 * Flow: render question -> user taps an option -> correct/incorrect colors
 * reveal on ALL options (not just the tapped one) -> further taps disabled
 * -> 1.5s delay -> auto-advance to next question, or show score summary if
 * this was the last question.
 *
 * Quiz data is passed in via static start() rather than Room, since quizzes
 * are NOT persisted -- they're regenerated fresh from notes each time.
 */
public class QuizActivity extends AppCompatActivity {

    private static final String EXTRA_SUBJECT_NAME = "subjectName";
    private static final String EXTRA_QUESTIONS_JSON = "questionsJson";
    private static final long AUTO_ADVANCE_DELAY_MS = 1500L;

    public static void start(Context context, String subjectName, List<QuizQuestion> questions) {
        String json = new Gson().toJson(questions);
        Intent intent = new Intent(context, QuizActivity.class);
        intent.putExtra(EXTRA_SUBJECT_NAME, subjectName);
        intent.putExtra(EXTRA_QUESTIONS_JSON, json);
        context.startActivity(intent);
    }

    private List<QuizQuestion> questions;
    private String subjectName;

    private int currentIndex = 0;
    private int score = 0;
    private boolean isAnswerLocked = false;
    private boolean isQuizComplete = false;

    private MaterialToolbar quizToolbar;
    private TextView questionCounterLabel;
    private TextView scoreLabel;
    private ProgressBar quizProgressBar;
    private TextView questionText;
    private LinearLayout optionsContainer;
    private TextView advancingLabel;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // System bar padding so the toolbar doesn't sit flush under the
        // status bar (matches the pattern used in HomeActivity/ChatActivity).
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        subjectName = getIntent().getStringExtra(EXTRA_SUBJECT_NAME);
        String questionsJson = getIntent().getStringExtra(EXTRA_QUESTIONS_JSON);

        questions = new Gson().fromJson(
                questionsJson,
                new TypeToken<List<QuizQuestion>>() {}.getType()
        );

        quizToolbar = findViewById(R.id.quizToolbar);
        questionCounterLabel = findViewById(R.id.questionCounterLabel);
        scoreLabel = findViewById(R.id.scoreLabel);
        quizProgressBar = findViewById(R.id.quizProgressBar);
        questionText = findViewById(R.id.questionText);
        optionsContainer = findViewById(R.id.optionsContainer);
        advancingLabel = findViewById(R.id.advancingLabel);

        quizToolbar.setTitle("Quiz");
        setSupportActionBar(quizToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        quizToolbar.setNavigationOnClickListener(v -> onBackPressed());

        if (questions == null || questions.isEmpty()) {
            // Defensive fallback -- shouldn't happen since generateQuiz()
            // already checks for null/empty before calling start(), but
            // guards against a future caller skipping that check.
            questionText.setText("No quiz questions were found. Please go back and try generating the quiz again.");
            optionsContainer.removeAllViews();
            return;
        }

        renderQuestion(currentIndex);
    }

    @Override
    public void onBackPressed() {
        if (isQuizComplete) {
            super.onBackPressed();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Quiz in progress")
                .setMessage("Your progress on this quiz won't be saved. Exit anyway?")
                .setPositiveButton("Exit", (dialog, which) -> super.onBackPressed())
                .setNegativeButton("Keep going", null)
                .show();
    }

    private void renderQuestion(int index) {
        isAnswerLocked = false;
        advancingLabel.setVisibility(View.INVISIBLE);

        QuizQuestion current = questions.get(index);

        int totalQuestions = questions.size();
        questionCounterLabel.setText("Question " + (index + 1) + " of " + totalQuestions);
        scoreLabel.setText("Score: " + score);

        int progressPercent = (int) (((index) / (float) totalQuestions) * 100);
        quizProgressBar.setProgress(progressPercent);

        questionText.setText(current.question);

        optionsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < current.options.size(); i++) {
            View pill = inflater.inflate(R.layout.item_quiz_option, optionsContainer, false);

            View root = pill.findViewById(R.id.optionPillRoot);
            TextView letterBadge = pill.findViewById(R.id.optionLetterBadge);
            TextView optionTextView = pill.findViewById(R.id.optionText);
            ImageView resultIcon = pill.findViewById(R.id.optionResultIcon);

            char letter = (char) ('A' + i);
            letterBadge.setText(String.valueOf(letter));
            optionTextView.setText(current.options.get(i));
            resultIcon.setVisibility(View.GONE);

            int selectedIndex = i;
            root.setOnClickListener(v -> handleOptionSelected(current, selectedIndex));

            optionsContainer.addView(pill);
        }
    }

    private void handleOptionSelected(QuizQuestion current, int selectedIndex) {
        if (isAnswerLocked) {
            return;
        }
        isAnswerLocked = true;

        boolean wasCorrect = selectedIndex == current.correctIndex;
        if (wasCorrect) {
            score++;
            scoreLabel.setText("Score: " + score);
        }

        // Reveal correct/incorrect state on every option, not just the
        // tapped one, so the user sees what the right answer was even if
        // they picked wrong.
        for (int i = 0; i < optionsContainer.getChildCount(); i++) {
            View pill = optionsContainer.getChildAt(i);
            View root = pill.findViewById(R.id.optionPillRoot);
            View badgeContainer = pill.findViewById(R.id.optionLetterBadgeContainer);
            TextView letterBadge = pill.findViewById(R.id.optionLetterBadge);
            ImageView resultIcon = pill.findViewById(R.id.optionResultIcon);

            root.setClickable(false);

            if (i == current.correctIndex) {
                root.setBackgroundResource(R.drawable.bg_quiz_option_correct);
                badgeContainer.setBackgroundResource(R.drawable.bg_quiz_letter_badge_correct);
                letterBadge.setText("");
                resultIcon.setVisibility(View.VISIBLE);
                resultIcon.setImageResource(R.drawable.ic_quiz_correct);
            } else if (i == selectedIndex) {
                root.setBackgroundResource(R.drawable.bg_quiz_option_incorrect);
                badgeContainer.setBackgroundResource(R.drawable.bg_quiz_letter_badge_incorrect);
                letterBadge.setText("");
                resultIcon.setVisibility(View.VISIBLE);
                resultIcon.setImageResource(R.drawable.ic_quiz_incorrect);
            } else {
                pill.setAlpha(0.5f);
            }
        }

        advancingLabel.setVisibility(View.VISIBLE);
        handler.postDelayed(this::advanceToNextQuestion, AUTO_ADVANCE_DELAY_MS);
    }

    private void advanceToNextQuestion() {
        currentIndex++;
        if (currentIndex >= questions.size()) {
            showSummary();
        } else {
            renderQuestion(currentIndex);
        }
    }

    private void showSummary() {
        android.util.Log.d("QuizActivity", "showSummary() called - score=" + score + " total=" + questions.size());
        isQuizComplete = true;
        quizProgressBar.setProgress(100);
        try {
            QuizSummaryActivity.start(this, subjectName, score, questions.size(), questions);
            android.util.Log.d("QuizActivity", "QuizSummaryActivity.start() returned without throwing");
        } catch (Exception e) {
            android.util.Log.e("QuizActivity", "Failed to launch QuizSummaryActivity", e);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel any pending auto-advance so it doesn't fire after the
        // activity is gone (e.g. user backs out during the delay window).
        handler.removeCallbacksAndMessages(null);
    }
}