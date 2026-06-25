package com.example.studybuddy.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "notes")
public class Note implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int subjectId;

    public String content;

    public String fileName;

    public long createdAt;
}