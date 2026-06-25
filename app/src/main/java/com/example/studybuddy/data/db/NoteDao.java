package com.example.studybuddy.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.studybuddy.data.model.Note;
import java.util.List;

@Dao
public interface NoteDao {

    @Insert
    void insert(Note note);

    @Update
    void update(Note note);

    @Query("SELECT * FROM notes WHERE subjectId = :subjectId ORDER BY createdAt DESC")
    LiveData<List<Note>> getNotesBySubject(int subjectId);

    @Query("SELECT content FROM notes WHERE subjectId = :subjectId")
    List<String> getNoteContentsBySubject(int subjectId);

    @Delete
    void delete(Note note);
}