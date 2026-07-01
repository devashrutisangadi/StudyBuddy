package com.example.studybuddy.ui.quiz.history;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.data.repository.QuizAttemptRepository;
import com.example.studybuddy.ui.BaseActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class QuizHistoryActivity extends BaseActivity {

    private static final String EXTRA_SUBJECT_ID = "subjectId";
    private static final String EXTRA_SUBJECT_NAME = "subjectName";

    public static void start(Context context, int subjectId, String subjectName) {
        Intent intent = new Intent(context, QuizHistoryActivity.class);
        intent.putExtra(EXTRA_SUBJECT_ID, subjectId);
        intent.putExtra(EXTRA_SUBJECT_NAME, subjectName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_history);

        int subjectId = getIntent().getIntExtra(EXTRA_SUBJECT_ID, -1);
        String subjectName = getIntent().getStringExtra(EXTRA_SUBJECT_NAME);

        MaterialToolbar toolbar = findViewById(R.id.historyToolbar);
        toolbar.setTitle(subjectName != null ? subjectName + " — History" : "Quiz History");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        RecyclerView rv = findViewById(R.id.rvHistory);
        TextView tvEmpty = findViewById(R.id.tvHistoryEmpty);

        QuizAttemptAdapter adapter = new QuizAttemptAdapter();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        if (subjectId == -1) {
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        new QuizAttemptRepository(getApplication())
                .getAttemptsBySubject(subjectId)
                .observe(this, attempts -> {
                    if (attempts == null || attempts.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rv.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        rv.setVisibility(View.VISIBLE);
                        adapter.setAttempts(attempts);
                    }
                });
    }
}