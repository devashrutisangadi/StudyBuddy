package com.example.studybuddy.data.repository;

import android.app.Application;
import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;

import com.example.studybuddy.data.db.AppDatabase;
import com.example.studybuddy.data.db.NoteDao;
import com.example.studybuddy.data.model.Note;
import com.example.studybuddy.utils.PdfExtractor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NoteRepository {

    private final NoteDao noteDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public NoteRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        noteDao = db.noteDao();
    }

    public void insertTextNote(int subjectId, String content) {
        executor.execute(() -> {
            Note note = new Note();
            note.subjectId = subjectId;
            note.content = content;
            // Use empty string, not null — Room's generated binder crashes
            // on bindString(null) for non-nullable String columns.
            note.fileName = "";
            note.createdAt = System.currentTimeMillis();
            noteDao.insert(note);
        });
    }

    public void insertPdfNote(int subjectId, Uri pdfUri, String fileName, Context context) {
        executor.execute(() -> {
            String extractedText = PdfExtractor.extractText(context, pdfUri);
            Note note = new Note();
            note.subjectId = subjectId;
            note.content = extractedText != null ? extractedText : "";
            note.fileName = fileName != null ? fileName : "";
            note.createdAt = System.currentTimeMillis();
            noteDao.insert(note);
        });
    }

    public void deleteNote(Note note) {
        executor.execute(() -> noteDao.delete(note));
    }

    public LiveData<List<Note>> getNotes(int subjectId) {
        return noteDao.getNotesBySubject(subjectId);
    }

    public Future<List<String>> getNoteContents(int subjectId) {
        return executor.submit(() -> noteDao.getNoteContentsBySubject(subjectId));
    }
}