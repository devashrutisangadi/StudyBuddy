package com.example.studybuddy.ui.notes;

import android.app.Application;
import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.studybuddy.data.model.Note;
import com.example.studybuddy.data.repository.NoteRepository;

import java.util.List;

public class NotesViewModel extends AndroidViewModel {

    private final NoteRepository noteRepository;

    public NotesViewModel(Application application) {
        super(application);
        noteRepository = new NoteRepository(application);
    }

    public void addTextNote(int subjectId, String content) {
        noteRepository.insertTextNote(subjectId, content);
    }

    public void addPdfNote(int subjectId, Uri pdfUri, String fileName, Context context) {
        noteRepository.insertPdfNote(subjectId, pdfUri, fileName, context);
    }

    public void deleteNote(Note note) {
        noteRepository.deleteNote(note);
    }

    public LiveData<List<Note>> getNotes(int subjectId) {
        return noteRepository.getNotes(subjectId);
    }
}