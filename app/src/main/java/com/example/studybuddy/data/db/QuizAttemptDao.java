package com.example.studybuddy.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.studybuddy.data.model.QuizAttempt;

import java.util.List;

@Dao
public interface QuizAttemptDao {

    @Insert
    void insert(QuizAttempt attempt);

    @Query("SELECT * FROM quiz_attempts WHERE subjectId = :subjectId ORDER BY timestamp DESC")
    LiveData<List<QuizAttempt>> getAttemptsBySubject(int subjectId);
}