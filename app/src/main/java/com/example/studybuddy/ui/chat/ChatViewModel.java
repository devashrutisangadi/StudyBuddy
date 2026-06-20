package com.example.studybuddy.ui.chat;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.studybuddy.data.model.ChatMessage;
import com.example.studybuddy.data.repository.ChatRepository;
import com.example.studybuddy.data.repository.NoteRepository;
import com.example.studybuddy.utils.PromptBuilder;

import java.util.List;
import java.util.concurrent.Executors;

public class ChatViewModel extends AndroidViewModel {

    private final ChatRepository chatRepository;
    private final NoteRepository noteRepository;

    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private int subjectId;

    public ChatViewModel(Application application) {
        super(application);
        chatRepository = new ChatRepository(application);
        noteRepository = new NoteRepository(application);
    }

    public void init(int subjectId) {
        this.subjectId = subjectId;
    }

    public LiveData<List<ChatMessage>> getMessages() {
        return chatRepository.getMessages(subjectId);
    }

    public void sendMessage(String userText) {
        ChatMessage userMsg = new ChatMessage();
        userMsg.subjectId = subjectId;
        userMsg.message = userText;
        userMsg.isUser = true;
        userMsg.timestamp = System.currentTimeMillis();
        chatRepository.insertMessage(userMsg);

        isLoading.setValue(true);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<String> notes = noteRepository.getNoteContents(subjectId).get();
                String prompt = PromptBuilder.buildQAPrompt(notes, userText);
                callGeminiAndSaveResponse(prompt);
            } catch (Exception e) {
                isLoading.postValue(false);
                saveErrorMessage("Something went wrong: " + e.getMessage());
            }
        });
    }

    public void generateQuiz() {
        isLoading.setValue(true);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<String> notes = noteRepository.getNoteContents(subjectId).get();

                if (notes == null || notes.isEmpty()) {
                    isLoading.postValue(false);
                    saveErrorMessage("Please add notes before generating a quiz.");
                    return;
                }

                String prompt = PromptBuilder.buildQuizPrompt(notes);
                callGeminiAndSaveResponse(prompt);
            } catch (Exception e) {
                isLoading.postValue(false);
                saveErrorMessage("Something went wrong: " + e.getMessage());
            }
        });
    }

    private void callGeminiAndSaveResponse(String prompt) {
        MutableLiveData<String> aiResponse = new MutableLiveData<>();

        // observeForever must run on the main thread
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            aiResponse.observeForever(response -> {
                if (response != null) {
                    saveAiMessage(response);
                    isLoading.postValue(false);
                }
            });
        });

        chatRepository.sendToGemini(prompt, aiResponse);
    }

    private void saveAiMessage(String text) {
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.subjectId = subjectId;
        aiMsg.message = text;
        aiMsg.isUser = false;
        aiMsg.timestamp = System.currentTimeMillis();
        chatRepository.insertMessage(aiMsg);
    }

    private void saveErrorMessage(String text) {
        saveAiMessage(text);
    }
}