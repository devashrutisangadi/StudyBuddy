package com.example.studybuddy.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.studybuddy.data.model.Subject;
import java.util.List;

@Dao
public interface SubjectDao {

    @Insert
    void insert(Subject subject);

    @Query("SELECT * FROM subjects ORDER BY createdAt DESC")
    LiveData<List<Subject>> getAllSubjects();

    @Delete
    void delete(Subject subject);
}