package com.example.studybuddy.data.model;

import java.util.List;

/**
 * A single parsed quiz question. Not a Room @Entity — this is purely an
 * in-memory model used while a quiz is being taken, built from Gemini's
 * JSON response. Quizzes are not persisted to the database.
 */
public class QuizQuestion {

    public String question;
    public List<String> options; // exactly 4 entries
    public int correctIndex;     // 0-based index into options

    public QuizQuestion() {
    }

    public QuizQuestion(String question, List<String> options, int correctIndex) {
        this.question = question;
        this.options = options;
        this.correctIndex = correctIndex;
    }
}