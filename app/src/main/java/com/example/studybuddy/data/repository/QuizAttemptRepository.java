package com.example.studybuddy.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.studybuddy.data.db.AppDatabase;
import com.example.studybuddy.data.db.QuizAttemptDao;
import com.example.studybuddy.data.model.QuizAttempt;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuizAttemptRepository {

    private final QuizAttemptDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public QuizAttemptRepository(Application application) {
        dao = AppDatabase.getInstance(application).quizAttemptDao();
    }

    public void insert(int subjectId, int score, int total, String missedIndices) {
        executor.execute(() -> {
            QuizAttempt attempt = new QuizAttempt();
            attempt.subjectId = subjectId;
            attempt.score = score;
            attempt.total = total;
            attempt.timestamp = System.currentTimeMillis();
            // Never null — Room's binder crashes on bindString(null)
            attempt.missedIndices = missedIndices != null ? missedIndices : "";
            dao.insert(attempt);
        });
    }

    public LiveData<List<QuizAttempt>> getAttemptsBySubject(int subjectId) {
        return dao.getAttemptsBySubject(subjectId);
    }
}