package com.example.studybuddy.ui.quiz;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.studybuddy.R;
import com.example.studybuddy.data.model.QuizQuestion;
import com.example.studybuddy.data.repository.QuizAttemptRepository;
import com.example.studybuddy.ui.BaseActivity;
import com.example.studybuddy.ui.quiz.history.QuizHistoryActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.Angle;
import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.Rotation;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.core.models.Size;
import nl.dionsegijn.konfetti.xml.KonfettiView;

/**
 * Score summary shown after the last question. Offers retaking the exact
 * same question set (no new Gemini call) or returning to the chat screen.
 */
public class QuizSummaryActivity extends BaseActivity {

    private static final String EXTRA_SUBJECT_ID = "subjectId";
    private static final String EXTRA_SUBJECT_NAME = "subjectName";
    private static final String EXTRA_SCORE = "score";
    private static final String EXTRA_TOTAL = "total";
    private static final String EXTRA_QUESTIONS_JSON = "questionsJson";
    private static final String EXTRA_MISSED_INDICES = "missedIndices";

    public static void start(Context context, int subjectId, String subjectName, int score, int total,
                             List<QuizQuestion> questions, String missedIndices) {
        String json = new Gson().toJson(questions);
        Intent intent = new Intent(context, QuizSummaryActivity.class);
        intent.putExtra(EXTRA_SUBJECT_ID, subjectId);
        intent.putExtra(EXTRA_SUBJECT_NAME, subjectName);
        intent.putExtra(EXTRA_SCORE, score);
        intent.putExtra(EXTRA_TOTAL, total);
        intent.putExtra(EXTRA_QUESTIONS_JSON, json);
        intent.putExtra(EXTRA_MISSED_INDICES, missedIndices != null ? missedIndices : "");
        context.startActivity(intent);
    }

    private String subjectName;
    private int subjectId;
    private List<QuizQuestion> questions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_summary);

        subjectId = getIntent().getIntExtra(EXTRA_SUBJECT_ID, -1);
        subjectName = getIntent().getStringExtra(EXTRA_SUBJECT_NAME);
        int score = getIntent().getIntExtra(EXTRA_SCORE, 0);
        int total = getIntent().getIntExtra(EXTRA_TOTAL, 0);
        String questionsJson = getIntent().getStringExtra(EXTRA_QUESTIONS_JSON);
        String missedIndices = getIntent().getStringExtra(EXTRA_MISSED_INDICES);
        if (missedIndices == null) missedIndices = "";

        questions = new Gson().fromJson(
                questionsJson,
                new TypeToken<List<QuizQuestion>>() {}.getType()
        );

        // Parse missedIndices string into a Set for O(1) lookup in renderStrip
        java.util.Set<Integer> missedSet = new java.util.HashSet<>();
        if (!missedIndices.isEmpty()) {
            for (String part : missedIndices.split(",")) {
                try { missedSet.add(Integer.parseInt(part.trim())); } catch (NumberFormatException ignored) {}
            }
        }

        // Persist this attempt to Room
        if (subjectId != -1) {
            new QuizAttemptRepository(getApplication()).insert(subjectId, score, total, missedIndices);
        }

        TextView subjectLabel = findViewById(R.id.summarySubjectLabel);
        TextView scoreText = findViewById(R.id.summaryScoreText);
        TextView percentText = findViewById(R.id.summaryPercentText);
        LinearLayout stripContainer = findViewById(R.id.summaryStripContainer);
        TextView retakeButton = findViewById(R.id.btnRetakeQuiz);
        TextView backButton = findViewById(R.id.btnBackToChat);
        TextView historyButton = findViewById(R.id.btnViewHistory);

        subjectLabel.setText(subjectName);
        scoreText.setText(score + " / " + total);

        int percent = total > 0 ? Math.round((score / (float) total) * 100) : 0;
        percentText.setText(percent + "% correct");

        if (percent >= 60) {
            showConfetti();
        }

        renderStrip(stripContainer, total, missedSet);

        retakeButton.setOnClickListener(v -> {
            QuizActivity.start(this, subjectId, subjectName, questions);
            finish();
        });

        backButton.setOnClickListener(v -> finish());

        historyButton.setOnClickListener(v ->
                QuizHistoryActivity.start(this, subjectId, subjectName));
    }

    /**
     * Renders `total` evenly-spaced bars, colored green (correct) or red
     * (incorrect) based on which indices appear in missedSet.
     */
    private void renderStrip(LinearLayout container, int total, java.util.Set<Integer> missedSet) {
        container.removeAllViews();
        for (int i = 0; i < total; i++) {
            View bar = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, dpToPx(8), 1f
            );
            int marginPx = dpToPx(3);
            params.setMargins(marginPx, 0, marginPx, 0);
            bar.setLayoutParams(params);
            bar.setBackgroundResource(missedSet.contains(i)
                    ? R.drawable.bg_quiz_strip_bar_incorrect
                    : R.drawable.bg_quiz_strip_bar_correct);
            container.addView(bar);
        }
    }

    /**
     * Fires a short confetti burst from the top-center of the screen.
     * Only called when the score is 60% or higher (see onCreate).
     *
     * Party's Kotlin constructor has default values for every parameter
     * except emitter, but Java can't use Kotlin default arguments, so
     * every field must be supplied explicitly in the exact declared order:
     * angle, spread, speed, maxSpeed, damping, size, colors, shapes,
     * timeToLive, fadeOutEnabled, position, delay, rotation, emitter.
     */
    private void showConfetti() {
        KonfettiView konfettiView = findViewById(R.id.confettiView);

        Party party = new Party(
                Angle.BOTTOM,
                60,
                30f,
                15f,
                0.9f,
                Arrays.asList(new Size(12, 5f, 0.2f)),
                Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0x6750A4, 0xb48def),
                // NOTE: Shape.Square/Shape.Circle are Kotlin `object` singletons.
                // .INSTANCE is the standard Kotlin->Java interop accessor, but this
                // specific line is unverified against Konfetti's actual Shape.kt
                // source. If this doesn't compile, try Shape.Square / Shape.Circle
                // directly (no .INSTANCE), or check Shape.kt in the library's
                // source jar (accessible via "Go to Declaration" in Android Studio).
                Arrays.asList(Shape.Square.INSTANCE, Shape.Circle.INSTANCE),
                2000L,
                true,
                new Position.Relative(0.5, 0.0),
                0,
                new Rotation(true, 1f, 0.5f, 8f, 1.5f),
                new Emitter(2, TimeUnit.SECONDS).perSecond(40)
        );

        konfettiView.start(party);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density);
    }
}