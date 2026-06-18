package com.example.studybuddy.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class Note {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int subjectId;

    public String content;

    public String fileName;

    public long createdAt;
}