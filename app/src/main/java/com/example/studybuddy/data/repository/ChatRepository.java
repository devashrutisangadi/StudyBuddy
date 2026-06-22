package com.example.studybuddy.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.studybuddy.BuildConfig;
import com.example.studybuddy.data.db.AppDatabase;
import com.example.studybuddy.data.db.ChatDao;
import com.example.studybuddy.data.model.ChatMessage;
import com.example.studybuddy.network.GeminiApiService;
import com.example.studybuddy.network.GeminiRequest;
import com.example.studybuddy.network.GeminiResponse;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRepository {

    private static final int MAX_RETRIES = 2;
    private static final long RETRY_DELAY_MS = 1500;

    private final ChatDao chatDao;
    private final GeminiApiService apiService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ChatRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        chatDao = db.chatDao();
        apiService = GeminiApiService.create();
    }

    public void insertMessage(ChatMessage message) {
        executor.execute(() -> chatDao.insert(message));
    }

    public void clearChat(int subjectId) {
        executor.execute(() -> chatDao.clearChat(subjectId));
    }

    public LiveData<List<ChatMessage>> getMessages(int subjectId) {
        return chatDao.getMessagesForSubject(subjectId);
    }

    public void sendToGemini(String prompt, MutableLiveData<String> responseLive) {
        attemptSendToGemini(prompt, responseLive, 0, false);
    }

    private void attemptSendToGemini(String prompt, MutableLiveData<String> responseLive, int attempt, boolean usingFallbackModel) {
        GeminiRequest request = new GeminiRequest(prompt);
        String model = usingFallbackModel ? "gemini-2.0-flash" : "gemini-2.5-flash";

        apiService.generateContent(model, BuildConfig.GEMINI_API_KEY, request)
                .enqueue(new Callback<GeminiResponse>() {
                    @Override
                    public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            responseLive.postValue(response.body().getResponseText());
                        } else if (isRetryableHttpError(response.code()) && attempt < MAX_RETRIES) {
                            retryAfterDelay(prompt, responseLive, attempt, usingFallbackModel);
                        } else if (isRetryableHttpError(response.code()) && !usingFallbackModel) {
                            // Primary model exhausted retries — try a fallback model once
                            attemptSendToGemini(prompt, responseLive, 0, true);
                        } else {
                            responseLive.postValue("The AI service returned an error (code "
                                    + response.code() + "). Please try again in a moment.");
                        }
                    }

                    @Override
                    public void onFailure(Call<GeminiResponse> call, Throwable t) {
                        boolean isTimeoutOrNetwork = t instanceof java.net.SocketTimeoutException
                                || t instanceof java.io.IOException;

                        if (isTimeoutOrNetwork && attempt < MAX_RETRIES) {
                            retryAfterDelay(prompt, responseLive, attempt, usingFallbackModel);
                        } else if (isTimeoutOrNetwork && !usingFallbackModel) {
                            attemptSendToGemini(prompt, responseLive, 0, true);
                        } else {
                            responseLive.postValue("Couldn't reach the AI service after "
                                    + (attempt + 1) + " attempt(s). Please check your connection and try again.");
                        }
                    }
                });
    }

    private void retryAfterDelay(String prompt, MutableLiveData<String> responseLive, int attempt, boolean usingFallbackModel) {
        executor.execute(() -> {
            try {
                Thread.sleep(RETRY_DELAY_MS);
            } catch (InterruptedException ignored) {
            }
            attemptSendToGemini(prompt, responseLive, attempt + 1, usingFallbackModel);
        });
    }

    private boolean isRetryableHttpError(int code) {
        // 503 Service Unavailable, 429 Too Many Requests, 500/502/504 — all worth retrying
        return code == 503 || code == 429 || code == 500 || code == 502 || code == 504;
    }
}