package com.example.studybuddy.ui.quiz;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studybuddy.R;
import com.example.studybuddy.data.model.QuizQuestion;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * MINIMAL PLACEHOLDER \u2014 displays parsed quiz questions as plain text so the
 * JSON generation -> parsing -> navigation pipeline is fully testable end to
 * end. This is intentionally NOT the final UI.
 *
 * NEXT STEP (picked up in a follow-up session): replace the body of
 * onCreate() with a proper one-question-at-a-time UI: clickable A/B/C/D
 * option cards, reveal correct/incorrect on tap, "Next question" navigation,
 * a final score screen. Follow the established design system (cream
 * background #FAF8F5, purple #6750A4 accent, rounded pill/card shapes \u2014
 * see activity_home.xml / activity_chat.xml for reference styling).
 *
 * Quiz data is passed in via static start() rather than Room, since quizzes
 * are NOT persisted \u2014 they're regenerated fresh from notes each time.
 */
public class QuizActivity extends AppCompatActivity {

    private static final String EXTRA_SUBJECT_NAME = "subjectName";
    private static final String EXTRA_QUESTIONS_JSON = "questionsJson";

    public static void start(Context context, String subjectName, List<QuizQuestion> questions) {
        String json = new Gson().toJson(questions);
        Intent intent = new Intent(context, QuizActivity.class);
        intent.putExtra(EXTRA_SUBJECT_NAME, subjectName);
        intent.putExtra(EXTRA_QUESTIONS_JSON, json);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TEMPORARY: plain TextView showing raw parsed data.
        // Replace with a proper layout (see class-level comment above).
        TextView textView = new TextView(this);
        textView.setPadding(32, 64, 32, 32);
        textView.setTextSize(14);

        String subjectName = getIntent().getStringExtra(EXTRA_SUBJECT_NAME);
        String questionsJson = getIntent().getStringExtra(EXTRA_QUESTIONS_JSON);

        List<QuizQuestion> questions = new Gson().fromJson(
                questionsJson,
                new TypeToken<List<QuizQuestion>>() {}.getType()
        );

        StringBuilder display = new StringBuilder();
        display.append("Quiz \u2014 ").append(subjectName).append("\n\n");
        display.append("(Placeholder screen \u2014 parsed successfully, ")
                .append(questions != null ? questions.size() : 0)
                .append(" questions)\n\n");

        if (questions != null) {
            int qNum = 1;
            for (QuizQuestion q : questions) {
                display.append(qNum++).append(". ").append(q.question).append("\n");
                char letter = 'A';
                for (String option : q.options) {
                    String marker = (q.options.indexOf(option) == q.correctIndex) ? "  [CORRECT]" : "";
                    display.append("   ").append(letter++).append(") ").append(option).append(marker).append("\n");
                }
                display.append("\n");
            }
        }

        textView.setText(display.toString());
        setContentView(textView);
    }
}