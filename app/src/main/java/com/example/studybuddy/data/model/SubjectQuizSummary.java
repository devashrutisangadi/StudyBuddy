package com.example.studybuddy.data.model;

/**
 * Lightweight aggregate result for displaying quiz progress on Home
 * screen folder cards. Not a Room @Entity — built by a GROUP BY query
 * in QuizAttemptDao and never persisted directly.
 *
 * bestScorePercent is pre-computed in SQL as MAX(score * 100 / total)
 * so the adapter doesn't need to do any division at bind time.
 */
public class SubjectQuizSummary {

    public int subjectId;
    public long lastAttemptTimestamp;  // millis, most recent attempt
    public int bestScorePercent;       // 0–100, best score across all attempts
}