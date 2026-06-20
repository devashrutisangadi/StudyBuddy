package com.example.studybuddy.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.ui.notes.AddNotesActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class ChatActivity extends AppCompatActivity {

    private ChatViewModel viewModel;
    private ChatAdapter adapter;

    private RecyclerView rvChat;
    private TextView tvEmptyChat;
    private ProgressBar progressBar;
    private EditText etMessage;
    private Button btnSend;

    private int subjectId;
    private String subjectName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            androidx.core.graphics.Insets ime = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime());
            int bottomPadding = Math.max(systemBars.bottom, ime.bottom);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding);
            return insets;
        });

        subjectId = getIntent().getIntExtra("subjectId", -1);
        subjectName = getIntent().getStringExtra("subjectName");

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(subjectName);
        setSupportActionBar(toolbar);

        rvChat = findViewById(R.id.rvChat);
        tvEmptyChat = findViewById(R.id.tvEmptyChat);
        progressBar = findViewById(R.id.progressBar);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        viewModel.init(subjectId);

        adapter = new ChatAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvChat.setLayoutManager(layoutManager);
        rvChat.setAdapter(adapter);

        viewModel.getMessages().observe(this, messages -> {
            adapter.setMessages(messages);
            if (messages == null || messages.isEmpty()) {
                tvEmptyChat.setVisibility(View.VISIBLE);
            } else {
                tvEmptyChat.setVisibility(View.GONE);
                rvChat.scrollToPosition(messages.size() - 1);
            }
        });

        viewModel.isLoading.observe(this, loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            btnSend.setEnabled(!loading);
        });

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "Type a question first", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.sendMessage(text);
            etMessage.setText("");
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Add Notes");
        menu.add("Generate Quiz");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals("Add Notes")) {
            Intent intent = new Intent(this, AddNotesActivity.class);
            intent.putExtra("subjectId", subjectId);
            intent.putExtra("subjectName", subjectName);
            startActivity(intent);
            return true;
        } else if (item.getTitle().equals("Generate Quiz")) {
            viewModel.generateQuiz();
            Toast.makeText(this, "Generating quiz from your notes...", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}