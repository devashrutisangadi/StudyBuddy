package com.example.studybuddy;

import android.app.Application;
import com.example.studybuddy.utils.PdfExtractor;

public class StudyBuddyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PdfExtractor.init(this);
    }
}