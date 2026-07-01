package com.example.studybuddy.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.studybuddy.data.model.ChatMessage;
import com.example.studybuddy.data.model.Note;
import com.example.studybuddy.data.model.QuizAttempt;
import com.example.studybuddy.data.model.Subject;

// Version bumped to 2 for the addition of the quiz_attempts table.
// fallbackToDestructiveMigration() is already in use, so existing
// local data (subjects, notes, chat history) will be wiped on first
// install after this change. Acceptable for a dev build; add a real
// Migration object here before any production release.
@Database(entities = {Subject.class, Note.class, ChatMessage.class, QuizAttempt.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract SubjectDao subjectDao();
    public abstract NoteDao noteDao();
    public abstract ChatDao chatDao();
    public abstract QuizAttemptDao quizAttemptDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "study_buddy_db"
            ).fallbackToDestructiveMigration().build();
        }
        return instance;
    }
}