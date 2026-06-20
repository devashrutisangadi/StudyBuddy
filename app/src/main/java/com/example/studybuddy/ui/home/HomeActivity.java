package com.example.studybuddy.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.studybuddy.R;
import com.example.studybuddy.data.model.Subject;
//import com.example.studybuddy.ui.chat.ChatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity {

    private HomeViewModel viewModel;
    private SubjectAdapter adapter;

    private RecyclerView rvSubjects;
    private TextView tvEmptyState;
    private FloatingActionButton fabAddSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        rvSubjects = findViewById(R.id.rvSubjects);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        fabAddSubject = findViewById(R.id.fabAddSubject);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        adapter = new SubjectAdapter(new SubjectAdapter.OnSubjectClickListener() {
            @Override
            public void onClick(Subject subject) {
                Intent intent = new Intent(HomeActivity.this, com.example.studybuddy.ui.notes.AddNotesActivity.class);
                intent.putExtra("subjectId", subject.id);
                intent.putExtra("subjectName", subject.name);
                startActivity(intent);
            }

            @Override
            public void onLongClick(Subject subject) {
                new AlertDialog.Builder(HomeActivity.this)
                        .setTitle("Delete subject?")
                        .setMessage("This will delete \"" + subject.name + "\" and all its notes and chat history.")
                        .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteSubject(subject))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        rvSubjects.setLayoutManager(new GridLayoutManager(this, 2));
        rvSubjects.setAdapter(adapter);

        viewModel.getAllSubjects().observe(this, subjects -> {
            adapter.setSubjects(subjects);
            if (subjects == null || subjects.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
            }
        });

        fabAddSubject.setOnClickListener(v -> showAddSubjectDialog());
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