package com.example.studybuddy.ui.notes;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.studybuddy.R;
import com.example.studybuddy.data.model.Note;
import com.example.studybuddy.data.repository.NoteRepository;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Full-screen viewer for a saved note. Text notes (no fileName) are
 * editable with a Save action. PDF-derived notes are view-only, since
 * editing the extracted text and saving it back would diverge from the
 * actual PDF without any indication that's happened — the fileName badge
 * and read-only field make clear this is a transcript of the PDF, not a
 * raw text note the user wrote themselves.
 */
public class NoteViewerActivity extends AppCompatActivity {

    private static final String EXTRA_NOTE = "note";

    public static void start(Context context, Note note) {
        Intent intent = new Intent(context, NoteViewerActivity.class);
        intent.putExtra(EXTRA_NOTE, note);
        context.startActivity(intent);
    }

    private Note note;
    private NoteRepository noteRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_viewer);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        note = (Note) getIntent().getSerializableExtra(EXTRA_NOTE);
        noteRepository = new NoteRepository((Application) getApplicationContext());

        MaterialToolbar toolbar = findViewById(R.id.noteViewerToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        TextView dateLabel = findViewById(R.id.noteViewerLabel);
        TextView typeBadge = findViewById(R.id.noteViewerTypeBadge);
        EditText contentField = findViewById(R.id.noteContentField);
        TextView saveButton = findViewById(R.id.btnSaveNote);

        if (note == null) {
            Toast.makeText(this, "Couldn't load this note.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String dateStr = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
                .format(note.createdAt);
        dateLabel.setText("Added " + dateStr);

        boolean isPdf = note.fileName != null && !note.fileName.trim().isEmpty();
        contentField.setText(note.content);

        if (isPdf) {
            toolbar.setTitle(note.fileName);
            typeBadge.setText("PDF");
            contentField.setFocusable(false);
            contentField.setCursorVisible(false);
            saveButton.setVisibility(View.GONE);
        } else {
            toolbar.setTitle("Note");
            typeBadge.setText("Text");
            saveButton.setOnClickListener(v -> {
                note.content = contentField.getText().toString();
                noteRepository.updateNote(note);
                Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }
}