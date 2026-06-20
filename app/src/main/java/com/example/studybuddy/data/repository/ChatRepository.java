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

    public LiveData<List<ChatMessage>> getMessages(int subjectId) {
        return chatDao.getMessagesForSubject(subjectId);
    }

    public void sendToGemini(String prompt, MutableLiveData<String> responseLive) {
        GeminiRequest request = new GeminiRequest(prompt);

        apiService.generateContent(BuildConfig.GEMINI_API_KEY, request)
                .enqueue(new Callback<GeminiResponse>() {
                    @Override
                    public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            responseLive.postValue(response.body().getResponseText());
                        } else {
                            responseLive.postValue("Error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<GeminiResponse> call, Throwable t) {
                        responseLive.postValue("Failed: " + t.getMessage());
                    }
                });
    }
}