package com.example.studybuddy.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.studybuddy.R;
import com.example.studybuddy.data.model.Subject;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Replaces the old AlertDialog-based subject options popup. Shows Open
 * Chat / Add Notes (Text) / Upload PDF / Delete Subject as a single
 * cream-and-purple bottom sheet, matching the rest of the app's design
 * system instead of a default system dialog.
 *
 * Delete subject does NOT delete immediately — it dismisses this sheet
 * and hands control back to the listener, which shows the existing
 * delete-confirmation AlertDialog (unchanged from before).
 */
public class SubjectActionsBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_SUBJECT_ID = "subjectId";
    private static final String ARG_SUBJECT_NAME = "subjectName";

    public interface Listener {
        void onOpenChat(Subject subject);
        void onAddNotesText(Subject subject);
        void onUploadPdf(Subject subject);
        void onDeleteRequested(Subject subject);
    }

    private Listener listener;
    private Subject subject;

    public static SubjectActionsBottomSheet newInstance(Subject subject) {
        SubjectActionsBottomSheet sheet = new SubjectActionsBottomSheet();
        Bundle args = new Bundle();
        args.putInt(ARG_SUBJECT_ID, subject.id);
        args.putString(ARG_SUBJECT_NAME, subject.name);
        sheet.setArguments(args);
        sheet.subject = subject;
        return sheet;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_subject_actions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView subjectNameView = view.findViewById(R.id.sheetSubjectName);
        String displayName = subject != null ? subject.name
                : (getArguments() != null ? getArguments().getString(ARG_SUBJECT_NAME) : "");
        subjectNameView.setText(displayName);

        view.findViewById(R.id.rowOpenChat).setOnClickListener(v -> {
            dismiss();
            if (listener != null && subject != null) {
                listener.onOpenChat(subject);
            }
        });

        view.findViewById(R.id.rowAddNotesText).setOnClickListener(v -> {
            dismiss();
            if (listener != null && subject != null) {
                listener.onAddNotesText(subject);
            }
        });

        view.findViewById(R.id.rowUploadPdf).setOnClickListener(v -> {
            dismiss();
            if (listener != null && subject != null) {
                listener.onUploadPdf(subject);
            }
        });

        view.findViewById(R.id.rowDeleteSubject).setOnClickListener(v -> {
            dismiss();
            if (listener != null && subject != null) {
                listener.onDeleteRequested(subject);
            }
        });
    }
}