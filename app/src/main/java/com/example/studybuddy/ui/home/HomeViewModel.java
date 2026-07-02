package com.example.studybuddy.ui.home;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.studybuddy.data.model.Subject;
import com.example.studybuddy.data.model.SubjectQuizSummary;
import com.example.studybuddy.data.repository.QuizAttemptRepository;
import com.example.studybuddy.data.repository.SubjectRepository;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final SubjectRepository subjectRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public HomeViewModel(Application application) {
        super(application);
        subjectRepository = new SubjectRepository(application);
        quizAttemptRepository = new QuizAttemptRepository(application);
    }

    public LiveData<List<Subject>> getAllSubjects() {
        return subjectRepository.getAllSubjects();
    }

    /**
     * Returns one summary row per subject that has at least one quiz attempt
     * (subjectId, lastAttemptTimestamp, bestScorePercent). Subjects with no
     * attempts are absent from the list — SubjectAdapter treats a missing
     * entry as "no quiz taken yet" and hides the progress label.
     */
    public LiveData<List<SubjectQuizSummary>> getQuizSummaries() {
        return quizAttemptRepository.getAllSubjectSummaries();
    }

    public void addSubject(String name) {
        Subject subject = new Subject();
        subject.name = name;
        subject.createdAt = System.currentTimeMillis();
        subjectRepository.insertSubject(subject);
    }

    /**
     * Renames an existing subject in place. Keeps the same id/createdAt
     * (and color in SubjectAdapter, which is based on list position, not
     * identity) — only the name field changes.
     */
    public void renameSubject(Subject subject, String newName) {
        subject.name = newName;
        subjectRepository.updateSubject(subject);
    }

    public void deleteSubject(Subject subject) {
        subjectRepository.deleteSubject(subject);
    }
}