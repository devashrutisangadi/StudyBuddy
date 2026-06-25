package com.example.studybuddy.ui.notes;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.google.android.material.appbar.MaterialToolbar;

public class AddNotesActivity extends AppCompatActivity {

    private NotesViewModel viewModel;
    private NoteAdapter noteAdapter;
    private int subjectId;
    private String subjectName;

    private TextView btnTextTab, btnPdfTab, btnNotesListTab;
    private TextView btnSaveText, btnPickPdf, btnUploadPdf;
    private CardView cardTextTab, cardPdfTab, cardNotesListTab;
    private LinearLayout layoutTextInput, layoutPdfInput, layoutNotesList;
    private EditText etNoteText;
    private TextView tvSelectedFile, tvNotesListEmpty;
    private RecyclerView rvNotesList;

    private Uri selectedPdfUri;
    private String selectedFileName;

    private static final int COLOR_ACTIVE = 0xFF6750A4;
    private static final int COLOR_INACTIVE = 0xFFFFFFFF;
    private static final int TEXT_ACTIVE = 0xFFFFFFFF;
    private static final int TEXT_INACTIVE = 0xFF6B6B6B;

    // Which of the 3 tabs is selected: 0 = text, 1 = pdf, 2 = notes list
    private int selectedTab = 0;

    private final ActivityResultLauncher<String[]> pdfPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    selectedPdfUri = uri;
                    selectedFileName = getFileNameFromUri(uri);
                    tvSelectedFile.setText(selectedFileName);
                    btnUploadPdf.setEnabled(true);
                    btnUploadPdf.setAlpha(1f);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notes);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottomPadding = Math.max(systemBars.bottom, ime.bottom);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding);
            return insets;
        });

        subjectId = getIntent().getIntExtra("subjectId", -1);
        subjectName = getIntent().getStringExtra("subjectName");

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Notes — " + subjectName);
        setSupportActionBar(toolbar);

        viewModel = new ViewModelProvider(this).get(NotesViewModel.class);

        cardTextTab = findViewById(R.id.cardTextTab);
        cardPdfTab = findViewById(R.id.cardPdfTab);
        cardNotesListTab = findViewById(R.id.cardNotesListTab);
        btnTextTab = findViewById(R.id.btnTextTab);
        btnPdfTab = findViewById(R.id.btnPdfTab);
        btnNotesListTab = findViewById(R.id.btnNotesListTab);
        btnSaveText = findViewById(R.id.btnSaveText);
        btnPickPdf = findViewById(R.id.btnPickPdf);
        btnUploadPdf = findViewById(R.id.btnUploadPdf);
        layoutTextInput = findViewById(R.id.layoutTextInput);
        layoutPdfInput = findViewById(R.id.layoutPdfInput);
        layoutNotesList = findViewById(R.id.layoutNotesList);
        etNoteText = findViewById(R.id.etNoteText);
        tvSelectedFile = findViewById(R.id.tvSelectedFile);
        tvNotesListEmpty = findViewById(R.id.tvNotesListEmpty);
        rvNotesList = findViewById(R.id.rvNotesList);

        btnUploadPdf.setEnabled(false);

        noteAdapter = new NoteAdapter(
                note -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Delete this note?")
                            .setMessage("This cannot be undone.")
                            .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteNote(note))
                            .setNegativeButton("Cancel", null)
                            .show();
                },
                note -> NoteViewerActivity.start(this, note)
        );
        rvNotesList.setLayoutManager(new LinearLayoutManager(this));
        rvNotesList.setAdapter(noteAdapter);

        viewModel.getNotes(subjectId).observe(this, notes -> {
            noteAdapter.setNotes(notes);
            tvNotesListEmpty.setVisibility(noteAdapter.isEmpty() ? View.VISIBLE : View.GONE);
        });

        String openTab = getIntent().getStringExtra("openTab");
        if ("pdf".equals(openTab)) {
            selectTab(1);
        } else {
            selectTab(0);
        }

        btnTextTab.setOnClickListener(v -> selectTab(0));
        btnPdfTab.setOnClickListener(v -> selectTab(1));
        btnNotesListTab.setOnClickListener(v -> selectTab(2));

        btnSaveText.setOnClickListener(v -> {
            String content = etNoteText.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(this, "Please enter some notes first", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.addTextNote(subjectId, content);
            Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show();
            etNoteText.setText("");
        });

        btnPickPdf.setOnClickListener(v ->
                pdfPickerLauncher.launch(new String[]{"application/pdf"}));

        btnUploadPdf.setOnClickListener(v -> {
            if (selectedPdfUri != null) {
                viewModel.addPdfNote(subjectId, selectedPdfUri, selectedFileName, this);
                Toast.makeText(this, "PDF uploaded and processed!", Toast.LENGTH_SHORT).show();
                tvSelectedFile.setText("No file selected");
                btnUploadPdf.setEnabled(false);
                btnUploadPdf.setAlpha(0.4f);
                selectedPdfUri = null;
            }
        });
    }

    /** Switches between the three tabs: 0 = text input, 1 = pdf input, 2 = notes list. */
    private void selectTab(int tab) {
        selectedTab = tab;

        layoutTextInput.setVisibility(tab == 0 ? View.VISIBLE : View.GONE);
        layoutPdfInput.setVisibility(tab == 1 ? View.VISIBLE : View.GONE);
        layoutNotesList.setVisibility(tab == 2 ? View.VISIBLE : View.GONE);

        cardTextTab.setCardBackgroundColor(tab == 0 ? COLOR_ACTIVE : COLOR_INACTIVE);
        btnTextTab.setTextColor(tab == 0 ? TEXT_ACTIVE : TEXT_INACTIVE);

        cardPdfTab.setCardBackgroundColor(tab == 1 ? COLOR_ACTIVE : COLOR_INACTIVE);
        btnPdfTab.setTextColor(tab == 1 ? TEXT_ACTIVE : TEXT_INACTIVE);

        cardNotesListTab.setCardBackgroundColor(tab == 2 ? COLOR_ACTIVE : COLOR_INACTIVE);
        btnNotesListTab.setTextColor(tab == 2 ? TEXT_ACTIVE : TEXT_INACTIVE);
    }

    private String getFileNameFromUri(Uri uri) {
        String result = "selected_file.pdf";
        try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    result = cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}