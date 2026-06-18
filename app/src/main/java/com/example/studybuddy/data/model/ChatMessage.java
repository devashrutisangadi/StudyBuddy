package com.example.studybuddy.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_messages")
public class ChatMessage {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int subjectId;

    public String message;

    public boolean isUser; // true = sent by user, false = sent by AI

    public long timestamp;
}