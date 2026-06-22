package com.example.studybuddy.network;

import com.example.studybuddy.data.model.QuizQuestion;

import java.util.List;

/**
 * Maps directly onto the JSON structure we ask Gemini to return for
 * quiz generation: { "questions": [ {question, options, correctIndex}, ... ] }
 * Parsed via Gson in QuizParser.
 */
public class QuizResponse {
    public List<QuizQuestion> questions;
}