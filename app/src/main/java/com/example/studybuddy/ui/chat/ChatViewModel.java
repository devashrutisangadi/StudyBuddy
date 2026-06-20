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
        // Save user's message immediately
        ChatMessage userMsg = new ChatMessage();
        userMsg.subjectId = subjectId;
        userMsg.message = userText;
        userMsg.isUser = true;
        userMsg.timestamp = System.currentTimeMillis();
        chatRepository.insertMessage(userMsg);

        isLoading.setValue(true);

        // Fetch notes + build prompt + call Gemini, all off the main thread
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<String> notes = noteRepository.getNoteContents(subjectId).get();
                String prompt = PromptBuilder.buildQAPrompt(notes, userText);

                MutableLiveData<String> aiResponse = new MutableLiveData<>();
                aiResponse.observeForever(response -> {
                    if (response != null) {
                        ChatMessage aiMsg = new ChatMessage();
                        aiMsg.subjectId = subjectId;
                        aiMsg.message = response;
                        aiMsg.isUser = false;
                        aiMsg.timestamp = System.currentTimeMillis();
                        chatRepository.insertMessage(aiMsg);
                        isLoading.postValue(false);
                    }
                });

                chatRepository.sendToGemini(prompt, aiResponse);

            } catch (Exception e) {
                isLoading.postValue(false);
            }
        });
    }

    public void generateQuiz() {
        isLoading.setValue(true);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<String> notes = noteRepository.getNoteContents(subjectId).get();
                String prompt = PromptBuilder.buildQuizPrompt(notes);

                MutableLiveData<String> aiResponse = new MutableLiveData<>();
                aiResponse.observeForever(response -> {
                    if (response != null) {
                        ChatMessage aiMsg = new ChatMessage();
                        aiMsg.subjectId = subjectId;
                        aiMsg.message = response;
                        aiMsg.isUser = false;
                        aiMsg.timestamp = System.currentTimeMillis();
                        chatRepository.insertMessage(aiMsg);
                        isLoading.postValue(false);
                    }
                });

                chatRepository.sendToGemini(prompt, aiResponse);

            } catch (Exception e) {
                isLoading.postValue(false);
            }
        });
    }
}