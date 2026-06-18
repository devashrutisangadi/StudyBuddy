package com.example.studybuddy.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.studybuddy.data.model.ChatMessage;
import java.util.List;

@Dao
public interface ChatDao {

    @Insert
    void insert(ChatMessage message);

    @Query("SELECT * FROM chat_messages WHERE subjectId = :subjectId ORDER BY timestamp ASC")
    LiveData<List<ChatMessage>> getMessagesForSubject(int subjectId);

    @Query("DELETE FROM chat_messages WHERE subjectId = :subjectId")
    void clearChat(int subjectId);
}