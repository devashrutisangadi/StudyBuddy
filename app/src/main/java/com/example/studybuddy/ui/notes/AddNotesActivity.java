package com.example.studybuddy.ui.notes;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.studybuddy.R;
import com.google.android.material.appbar.MaterialToolbar;

public class AddNotesActivity extends AppCompatActivity {

    private NotesViewModel viewModel;
    private int subjectId;
    private String subjectName;

    private Button btnTextTab, btnPdfTab, btnSaveText, btnPickPdf, btnUploadPdf;
    private android.widget.LinearLayout layoutTextInput, layoutPdfInput;
    private EditText etNoteText;
    private TextView tvSelectedFile;

    private Uri selectedPdfUri;
    private String selectedFileName;

    private final ActivityResultLauncher<String[]> pdfPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    selectedPdfUri = uri;
                    selectedFileName = getFileNameFromUri(uri);
                    tvSelectedFile.setText(selectedFileName);
                    btnUploadPdf.setEnabled(true);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notes);

        subjectId = getIntent().getIntExtra("subjectId", -1);
        subjectName = getIntent().getStringExtra("subjectName");

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Add Notes — " + subjectName);
        setSupportActionBar(toolbar);

        viewModel = new ViewModelProvider(this).get(NotesViewModel.class);

        btnTextTab = findViewById(R.id.btnTextTab);
        btnPdfTab = findViewById(R.id.btnPdfTab);
        btnSaveText = findViewById(R.id.btnSaveText);
        btnPickPdf = findViewById(R.id.btnPickPdf);
        btnUploadPdf = findViewById(R.id.btnUploadPdf);
        layoutTextInput = findViewById(R.id.layoutTextInput);
        layoutPdfInput = findViewById(R.id.layoutPdfInput);
        etNoteText = findViewById(R.id.etNoteText);
        tvSelectedFile = findViewById(R.id.tvSelectedFile);
        String openTab = getIntent().getStringExtra("openTab");
        if ("pdf".equals(openTab)) {
            layoutTextInput.setVisibility(View.GONE);
            layoutPdfInput.setVisibility(View.VISIBLE);
        }

        btnTextTab.setOnClickListener(v -> {
            layoutTextInput.setVisibility(View.VISIBLE);
            layoutPdfInput.setVisibility(View.GONE);
        });

        btnPdfTab.setOnClickListener(v -> {
            layoutTextInput.setVisibility(View.GONE);
            layoutPdfInput.setVisibility(View.VISIBLE);
        });

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
                selectedPdfUri = null;
            }
        });
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