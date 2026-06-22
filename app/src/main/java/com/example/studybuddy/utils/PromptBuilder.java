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

        return "Based on the notes below, generate exactly 5 multiple choice questions " +
                "to help the student study.\n\n" +
                "Respond with ONLY valid JSON, no markdown code fences, no preamble, " +
                "no explanation \u2014 just the raw JSON. Use exactly this structure:\n\n" +
                "{\n" +
                "  \"questions\": [\n" +
                "    {\n" +
                "      \"question\": \"question text here\",\n" +
                "      \"options\": [\"option A text\", \"option B text\", \"option C text\", \"option D text\"],\n" +
                "      \"correctIndex\": 0\n" +
                "    }\n" +
                "  ]\n" +
                "}\n\n" +
                "Rules:\n" +
                "- \"options\" must always have exactly 4 entries, in order (these correspond to A, B, C, D).\n" +
                "- \"correctIndex\" is the 0-based index (0=A, 1=B, 2=C, 3=D) of the correct option.\n" +
                "- Do not include the letter prefix (e.g. \"A)\") inside the option text itself.\n" +
                "- Base every question strictly on the notes below.\n\n" +
                "NOTES:\n" + notes;
    }
}