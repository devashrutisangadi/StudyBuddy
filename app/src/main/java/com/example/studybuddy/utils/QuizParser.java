package com.example.studybuddy.utils;

import com.example.studybuddy.data.model.QuizQuestion;
import com.example.studybuddy.network.QuizResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.List;

public class QuizParser {

    /**
     * Parses Gemini's raw text response into a list of QuizQuestion objects.
     * Defensively strips markdown code fences in case the model wraps the
     * JSON in ```json ... ``` despite being asked not to.
     *
     * @return the parsed list, or null if parsing failed (caller should
     *         show a friendly error and let the user retry).
     */
    public static List<QuizQuestion> parse(String rawResponse) {
        if (rawResponse == null) return null;

        String cleaned = stripCodeFences(rawResponse.trim());

        try {
            Gson gson = new Gson();
            QuizResponse parsed = gson.fromJson(cleaned, QuizResponse.class);

            if (parsed == null || parsed.questions == null || parsed.questions.isEmpty()) {
                return null;
            }

            // Defensive validation: drop any malformed question rather than
            // crashing the whole quiz on one bad entry.
            parsed.questions.removeIf(q ->
                    q.question == null
                            || q.options == null
                            || q.options.size() != 4
                            || q.correctIndex < 0
                            || q.correctIndex > 3
            );

            return parsed.questions.isEmpty() ? null : parsed.questions;

        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    private static String stripCodeFences(String text) {
        if (text.startsWith("```")) {
            int firstNewline = text.indexOf('\n');
            if (firstNewline != -1) {
                text = text.substring(firstNewline + 1);
            }
            int lastFence = text.lastIndexOf("```");
            if (lastFence != -1) {
                text = text.substring(0, lastFence);
            }
        }
        return text.trim();
    }
}