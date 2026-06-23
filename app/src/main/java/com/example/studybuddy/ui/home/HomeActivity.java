package com.example.studybuddy.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.data.model.Subject;
import com.example.studybuddy.ui.chat.ChatActivity;
import com.example.studybuddy.ui.notes.AddNotesActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity implements SubjectActionsBottomSheet.Listener {

    private HomeViewModel viewModel;
    private SubjectAdapter adapter;

    private RecyclerView rvSubjects;
    private TextView tvEmptyState;
    private FloatingActionButton fabAddSubject;
    private EditText etSearchSubjects;

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

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

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
    public void onDeleteRequested(Subject subject) {
        new AlertDialog.Builder(this)
                .setTitle("Delete subject?")
                .setMessage("This will delete \"" + subject.name + "\" and all its notes and chat history.")
                .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteSubject(subject))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddSubjectDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("e.g. Biology");

        new AlertDialog.Builder(this)
                .setTitle("New Subject")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        viewModel.addSubject(name);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}