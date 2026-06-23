package com.example.studybuddy.ui.chat;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
    private TextView btnSend;
    private ImageButton btnChatMenu;

    private int subjectId;
    private String subjectName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // System bar + IME-aware padding so the input bar stays above the keyboard
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
        toolbar.setTitle(subjectName);
        setSupportActionBar(toolbar);

        rvChat = findViewById(R.id.rvChat);
        tvEmptyChat = findViewById(R.id.tvEmptyChat);
        progressBar = findViewById(R.id.progressBar);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnChatMenu = findViewById(R.id.btnChatMenu);

        btnChatMenu.setOnClickListener(this::showChatMenu);

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
            btnSend.setAlpha(loading ? 0.5f : 1f);
        });

        viewModel.quizResult.observe(this, questions -> {
            if (questions != null && !questions.isEmpty()) {
                com.example.studybuddy.ui.quiz.QuizActivity.start(this, subjectName, questions);
            }
        });

        viewModel.quizError.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
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

    /**
     * Shows the custom cream/purple dropdown menu anchored under the
     * toolbar's overflow button, replacing the old system options menu.
     */
    private void showChatMenu(View anchor) {
        View menuView = LayoutInflater.from(this).inflate(R.layout.dropdown_chat_menu, null);

        PopupWindow popupWindow = new PopupWindow(
                menuView,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        popupWindow.setElevation(8f);

        menuView.findViewById(R.id.menuRowAddNotes).setOnClickListener(v -> {
            popupWindow.dismiss();
            Intent intent = new Intent(this, AddNotesActivity.class);
            intent.putExtra("subjectId", subjectId);
            intent.putExtra("subjectName", subjectName);
            startActivity(intent);
        });

        menuView.findViewById(R.id.menuRowGenerateQuiz).setOnClickListener(v -> {
            popupWindow.dismiss();
            viewModel.generateQuiz();
            Toast.makeText(this, "Generating quiz from your notes...", Toast.LENGTH_SHORT).show();
        });

        menuView.findViewById(R.id.menuRowClearChat).setOnClickListener(v -> {
            popupWindow.dismiss();
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Clear chat history?")
                    .setMessage("This will delete all messages in this chat. Your notes will not be affected.")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        viewModel.clearChat();
                        Toast.makeText(this, "Chat cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Anchor below-right of the button, nudged left so the menu's right
        // edge aligns with the button's right edge instead of overflowing
        // off the right side of the screen.
        popupWindow.showAsDropDown(anchor, -160, 8, Gravity.END);
    }
}