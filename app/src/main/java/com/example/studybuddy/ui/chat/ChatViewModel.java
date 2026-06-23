package com.example.studybuddy.ui.chat;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.studybuddy.data.model.ChatMessage;
import com.example.studybuddy.data.model.QuizQuestion;
import com.example.studybuddy.data.repository.ChatRepository;
import com.example.studybuddy.data.repository.NoteRepository;
import com.example.studybuddy.utils.PromptBuilder;
import com.example.studybuddy.utils.QuizParser;

import java.util.List;
import java.util.concurrent.Executors;

public class ChatViewModel extends AndroidViewModel {

    private final ChatRepository chatRepository;
    private final NoteRepository noteRepository;

    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    /** Emits a parsed quiz when generation succeeds. ChatActivity observes
     *  this to launch QuizActivity. Emits null + a toast-worthy message via
     *  quizError when generation or parsing fails. */
    public final MutableLiveData<List<QuizQuestion>> quizResult = new MutableLiveData<>();
    public final MutableLiveData<String> quizError = new MutableLiveData<>();

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

    public void clearChat() {
        chatRepository.clearChat(subjectId);
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

    /**
     * Generates a quiz and emits the parsed result via quizResult (success)
     * or quizError (failure) \u2014 does NOT write anything to the chat history,
     * since the quiz now lives in its own screen.
     */
    public void generateQuiz() {
        isLoading.setValue(true);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<String> notes = noteRepository.getNoteContents(subjectId).get();

                if (notes == null || notes.isEmpty()) {
                    isLoading.postValue(false);
                    quizError.postValue("Please add notes before generating a quiz.");
                    return;
                }

                String prompt = PromptBuilder.buildQuizPrompt(notes);
                MutableLiveData<String> aiResponse = new MutableLiveData<>();

                new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                        aiResponse.observeForever(response -> {
                            isLoading.postValue(false);
                            android.util.Log.d("QuizDebug", "Raw Gemini response: [" + response + "]");
                            if (response == null) {
                                quizError.postValue("No response from the AI service.");
                                return;
                            }

                            List<QuizQuestion> parsed = QuizParser.parse(response);
                            if (parsed == null) {
                                quizError.postValue("Couldn't generate a quiz right now. Please try again.");
                            } else {
                                quizResult.postValue(parsed);
                            }
                        })
                );

                chatRepository.sendToGemini(prompt, aiResponse);

            } catch (Exception e) {
                isLoading.postValue(false);
                quizError.postValue("Something went wrong: " + e.getMessage());
            }
        });
    }

    private void callGeminiAndSaveResponse(String prompt) {
        MutableLiveData<String> aiResponse = new MutableLiveData<>();

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