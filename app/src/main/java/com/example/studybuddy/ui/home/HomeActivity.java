package com.example.studybuddy.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.data.model.Subject;
import com.example.studybuddy.ui.BaseActivity;
import com.example.studybuddy.ui.chat.ChatActivity;
import com.example.studybuddy.ui.notes.AddNotesActivity;
import com.example.studybuddy.ui.quiz.QuizActivity;
import com.example.studybuddy.ui.settings.SettingsActivity;
import com.example.studybuddy.utils.QuizGenerator;
import com.example.studybuddy.utils.StyledDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends BaseActivity implements SubjectActionsBottomSheet.Listener {

    private HomeViewModel viewModel;
    private SubjectAdapter adapter;
    private QuizGenerator quizGenerator;

    private RecyclerView rvSubjects;
    private TextView tvEmptyState;
    private FloatingActionButton fabAddSubject;
    private EditText etSearchSubjects;
    private ImageButton btnSettings;

    // Tracks which subject a quiz is currently being generated for, since
    // quizGenerator.generateQuiz() takes a subjectId but the result/error
    // LiveData callbacks need the subject's NAME too (for QuizActivity.start
    // and for showing which subject failed).
    private Subject pendingQuizSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Apply system bar padding so content doesn't touch status bar / nav bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvSubjects = findViewById(R.id.rvSubjects);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        fabAddSubject = findViewById(R.id.fabAddSubject);
        etSearchSubjects = findViewById(R.id.etSearchSubjects);
        btnSettings = findViewById(R.id.btnSettings);

        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        quizGenerator = new QuizGenerator(getApplication());
        quizGenerator.isLoading.observe(this, loading -> {
            // No dedicated loading UI on this screen yet — a Toast on start
            // is shown from onGenerateQuiz() below. Hook a ProgressBar here
            // later if a more visible loading state is wanted.
        });
        quizGenerator.quizResult.observe(this, questions -> {
            if (questions != null && !questions.isEmpty() && pendingQuizSubject != null) {
                QuizActivity.start(this, pendingQuizSubject.name, questions);
                pendingQuizSubject = null;
            }
        });
        quizGenerator.quizError.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                pendingQuizSubject = null;
            }
        });

        adapter = new SubjectAdapter(new SubjectAdapter.OnSubjectClickListener() {
            @Override
            public void onClick(Subject subject) {
                showSubjectActionsSheet(subject);
            }

            @Override
            public void onLongClick(Subject subject) {
                // Long-press no longer does anything special — all actions,
                // including delete, are reached via the tap-triggered
                // bottom sheet now.
            }
        });

        rvSubjects.setLayoutManager(new GridLayoutManager(this, 2));
        rvSubjects.setAdapter(adapter);

        viewModel.getAllSubjects().observe(this, subjects -> {
            adapter.setSubjects(subjects);
            updateEmptyState();
        });

        fabAddSubject.setOnClickListener(v -> showAddSubjectDialog());

        etSearchSubjects.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
                updateEmptyState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updateEmptyState() {
        tvEmptyState.setVisibility(adapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showSubjectActionsSheet(Subject subject) {
        SubjectActionsBottomSheet sheet = SubjectActionsBottomSheet.newInstance(subject);
        sheet.setListener(this);
        sheet.show(getSupportFragmentManager(), "SubjectActionsBottomSheet");
    }

    // --- SubjectActionsBottomSheet.Listener ---

    @Override
    public void onOpenChat(Subject subject) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("subjectId", subject.id);
        intent.putExtra("subjectName", subject.name);
        startActivity(intent);
    }

    @Override
    public void onGenerateQuiz(Subject subject) {
        pendingQuizSubject = subject;
        quizGenerator.generateQuiz(subject.id);
        Toast.makeText(this, "Generating quiz from your notes...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAddNotesText(Subject subject) {
        Intent intent = new Intent(this, AddNotesActivity.class);
        intent.putExtra("subjectId", subject.id);
        intent.putExtra("subjectName", subject.name);
        intent.putExtra("openTab", "text");
        startActivity(intent);
    }

    @Override
    public void onUploadPdf(Subject subject) {
        Intent intent = new Intent(this, AddNotesActivity.class);
        intent.putExtra("subjectId", subject.id);
        intent.putExtra("subjectName", subject.name);
        intent.putExtra("openTab", "pdf");
        startActivity(intent);
    }

    @Override
    public void onRenameRequested(Subject subject) {
        StyledDialog.input(this, R.drawable.ic_dialog_rename, "Rename subject",
                "e.g. Biology", subject.name, "Save", "Cancel", newName -> {
                    if (!newName.isEmpty() && !newName.equals(subject.name)) {
                        viewModel.renameSubject(subject, newName);
                    }
                });
    }

    @Override
    public void onDeleteRequested(Subject subject) {
        StyledDialog.confirmDanger(this, R.drawable.ic_sheet_delete,
                "Delete subject?",
                "This will delete \"" + subject.name + "\" and all its notes and chat history. This can't be undone.",
                "Delete", "Cancel",
                () -> viewModel.deleteSubject(subject));
    }

    private void showAddSubjectDialog() {
        StyledDialog.input(this, "New subject", "e.g. Biology", "Add", "Cancel", name -> {
            if (!name.isEmpty()) {
                viewModel.addSubject(name);
            }
        });
    }
}