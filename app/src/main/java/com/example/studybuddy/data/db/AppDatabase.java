package com.example.studybuddy.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.studybuddy.data.model.ChatMessage;
import com.example.studybuddy.data.model.Note;
import com.example.studybuddy.data.model.Subject;

@Database(entities = {Subject.class, Note.class, ChatMessage.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract SubjectDao subjectDao();
    public abstract NoteDao noteDao();
    public abstract ChatDao chatDao();

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