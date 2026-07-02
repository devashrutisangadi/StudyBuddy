package com.example.studybuddy.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.studybuddy.data.model.QuizAttempt;
import com.example.studybuddy.data.model.SubjectQuizSummary;

import java.util.List;

@Dao
public interface QuizAttemptDao {

    @Insert
    void insert(QuizAttempt attempt);

    @Query("SELECT * FROM quiz_attempts WHERE subjectId = :subjectId ORDER BY timestamp DESC")
    LiveData<List<QuizAttempt>> getAttemptsBySubject(int subjectId);

    /**
     * Returns one summary row per subject that has at least one attempt.
     * Column aliases must match SubjectQuizSummary field names exactly
     * so Room's cursor-to-POJO mapping works without a TypeConverter.
     *
     * bestScorePercent: integer division is fine here — we only need
     * whole-number percentages for the card label ("Best: 80%").
     * MAX() over score*100/total gives the best attempt's percent.
     */
    @Query("SELECT subjectId, " +
            "MAX(timestamp) AS lastAttemptTimestamp, " +
            "MAX(score * 100 / total) AS bestScorePercent " +
            "FROM quiz_attempts " +
            "GROUP BY subjectId")
    LiveData<List<SubjectQuizSummary>> getAllSubjectSummaries();
}