package com.example.studybuddy.ui.home;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.studybuddy.data.model.Subject;
import com.example.studybuddy.data.repository.SubjectRepository;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final SubjectRepository subjectRepository;

    public HomeViewModel(Application application) {
        super(application);
        subjectRepository = new SubjectRepository(application);
    }

    public LiveData<List<Subject>> getAllSubjects() {
        return subjectRepository.getAllSubjects();
    }

    public void addSubject(String name) {
        Subject subject = new Subject();
        subject.name = name;
        subject.createdAt = System.currentTimeMillis();
        subjectRepository.insertSubject(subject);
    }

    public void deleteSubject(Subject subject) {
        subjectRepository.deleteSubject(subject);
    }
}