package com.example.studybuddy.utils;

import java.util.List;

public class PromptBuilder {

    public static String buildQAPrompt(List<String> noteContents, String userQuestion) {
        StringBuilder notes = new StringBuilder();
        for (String content : noteContents) {
            notes.append(content).append("\n\n");
        }

        return "You are a study assistant. Answer the student's question using ONLY " +
                "the notes provided below. If the answer is not in the notes, say " +
                "'I couldn't find that in your notes.'\n\n" +
                "NOTES:\n" + notes + "\n\n" +
                "QUESTION: " + userQuestion + "\n\n" +
                "ANSWER:";
    }

    public static String buildQuizPrompt(List<String> noteContents) {
        StringBuilder notes = new StringBuilder();
        for (String content : noteContents) {
            notes.append(content).append("\n\n");
        }

        return "Based on the notes below, generate 5 multiple choice questions to help " +
                "the student study. Format each question as:\n\n" +
                "Q: question text\n" +
                "A) option\n" +
                "B) option\n" +
                "C) option\n" +
                "D) option\n" +
                "Answer: correct option letter\n\n" +
                "NOTES:\n" + notes;
    }
}