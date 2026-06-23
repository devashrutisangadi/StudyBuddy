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
        // gemini-2.0-flash was deprecated and shut down June 1, 2026 -- using
        // gemini-2.5-flash-lite as the fallback instead, since it's a separate
        // live model with its own quota pool from the primary 2.5-flash.
        String model = usingFallbackModel ? "gemini-2.5-flash-lite" : "gemini-2.5-flash";

        apiService.generateContent(model, BuildConfig.GEMINI_API_KEY, request)
                .enqueue(new Callback<GeminiResponse>() {
                    @Override
                    public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                        int code = response.code();

                        if (response.isSuccessful() && response.body() != null) {
                            responseLive.postValue(response.body().getResponseText());
                        } else if (isQuotaError(code) && !usingFallbackModel) {
                            // 429 means OUR quota is exhausted for this specific
                            // model -- retrying the same model is pointless, but
                            // a different model has a separate quota pool, so
                            // it's worth trying once before giving up.
                            attemptSendToGemini(prompt, responseLive, 0, true);
                        } else if (isQuotaError(code) && usingFallbackModel) {
                            // Both models are rate-limited -- no point retrying
                            // either one right now.
                            responseLive.postValue("You've hit the AI service's rate limit. Please wait a bit before trying again.");
                        } else if (isTransientServerError(code) && attempt < MAX_RETRIES) {
                            retryAfterDelay(prompt, responseLive, attempt, usingFallbackModel);
                        } else if (isTransientServerError(code) && !usingFallbackModel) {
                            // Primary model exhausted retries — try a fallback model once
                            attemptSendToGemini(prompt, responseLive, 0, true);
                        } else {
                            responseLive.postValue("The AI service returned an error (code "
                                    + code + "). Please try again in a moment.");
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

    /**
     * 429 Too Many Requests -- this means YOUR quota is exhausted, not a
     * transient server issue. Retrying the same model immediately just
     * burns more of an already-exhausted quota, so this is handled
     * separately from transient server errors (no same-model retry).
     */
    private boolean isQuotaError(int code) {
        return code == 429;
    }

    /**
     * 503 Service Unavailable, 500/502/504 -- genuine transient server-side
     * issues worth retrying with backoff, since the problem is on Google's
     * end, not your quota.
     */
    private boolean isTransientServerError(int code) {
        return code == 503 || code == 500 || code == 502 || code == 504;
    }
}