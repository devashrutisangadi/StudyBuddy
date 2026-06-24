package com.example.studybuddy.utils;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;

import com.example.studybuddy.data.model.QuizQuestion;
import com.example.studybuddy.data.repository.ChatRepository;
import com.example.studybuddy.data.repository.NoteRepository;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * Shared quiz-generation logic, usable from any screen (ChatActivity,
 * HomeActivity, etc.) without depending on ChatViewModel or any
 * Activity-specific state.
 *
 * Both NoteRepository and ChatRepository only need an Application context
 * and take subjectId/prompt as parameters, so this class can be
 * instantiated anywhere a quiz needs to be generated.
 *
 * Usage:
 *     QuizGenerator generator = new QuizGenerator(application);
 *     generator.isLoading.observe(owner, loading -> ...);
 *     generator.quizResult.observe(owner, questions -> ...);
 *     generator.quizError.observe(owner, error -> ...);
 *     generator.generateQuiz(subjectId);
 */
public class QuizGenerator {

    private final ChatRepository chatRepository;
    private final NoteRepository noteRepository;

    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    public final MutableLiveData<List<QuizQuestion>> quizResult = new MutableLiveData<>();
    public final MutableLiveData<String> quizError = new MutableLiveData<>();

    public QuizGenerator(Application application) {
        chatRepository = new ChatRepository(application);
        noteRepository = new NoteRepository(application);
    }

    public void generateQuiz(int subjectId) {
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
}