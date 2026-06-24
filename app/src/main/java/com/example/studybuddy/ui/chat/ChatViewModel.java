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
import com.example.studybuddy.utils.QuizGenerator;

import java.util.List;
import java.util.concurrent.Executors;

public class ChatViewModel extends AndroidViewModel {

    private final ChatRepository chatRepository;
    private final NoteRepository noteRepository;
    private final QuizGenerator quizGenerator;

    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    /** Emits a parsed quiz when generation succeeds. ChatActivity observes
     *  this to launch QuizActivity. Emits null + a toast-worthy message via
     *  quizError when generation or parsing fails. Backed by the shared
     *  QuizGenerator so HomeActivity can trigger the same logic independently. */
    public final MutableLiveData<List<QuizQuestion>> quizResult;
    public final MutableLiveData<String> quizError;

    private int subjectId;

    public ChatViewModel(Application application) {
        super(application);
        chatRepository = new ChatRepository(application);
        noteRepository = new NoteRepository(application);
        quizGenerator = new QuizGenerator(application);
        quizResult = quizGenerator.quizResult;
        quizError = quizGenerator.quizError;
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
     * Generates a quiz via the shared QuizGenerator and emits the parsed
     * result through quizResult (success) or quizError (failure) — does NOT
     * write anything to the chat history, since the quiz lives in its own
     * screen.
     */
    public void generateQuiz() {
        quizGenerator.generateQuiz(subjectId);
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