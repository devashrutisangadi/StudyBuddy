package com.example.studybuddy.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Persisted record of a single completed quiz attempt.
 *
 * missedIndices stores the 0-based indices of questions the user
 * answered incorrectly, as a comma-separated String (e.g. "0,2,4").
 * Empty string means a perfect score. Room can't store List<Integer>
 * natively without a TypeConverter, so the flat String approach
 * matches this project's existing no-converter convention.
 *
 * Note: same empty-string-not-null discipline as Note.fileName —
 * Room's generated binder crashes on bindString(null) for non-nullable
 * String columns, so missedIndices must always be "" not null.
 */
@Entity(tableName = "quiz_attempts")
public class QuizAttempt {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int subjectId;

    public int score;

    public int total;

    public long timestamp;

    public String missedIndices; // comma-separated, e.g. "0,2,4" or ""
}