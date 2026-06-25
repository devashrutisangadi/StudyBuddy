package com.example.studybuddy.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.studybuddy.data.db.AppDatabase;
import com.example.studybuddy.data.db.SubjectDao;
import com.example.studybuddy.data.model.Subject;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubjectRepository {

    private final SubjectDao subjectDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public SubjectRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        subjectDao = db.subjectDao();
    }

    public void insertSubject(Subject subject) {
        executor.execute(() -> subjectDao.insert(subject));
    }

    public void updateSubject(Subject subject) {
        executor.execute(() -> subjectDao.update(subject));
    }

    public LiveData<List<Subject>> getAllSubjects() {
        return subjectDao.getAllSubjects();
    }

    public void deleteSubject(Subject subject) {
        executor.execute(() -> subjectDao.delete(subject));
    }
}