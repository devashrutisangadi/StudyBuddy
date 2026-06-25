package com.example.studybuddy.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.studybuddy.R;

/**
 * Cream/purple-themed replacement for plain AlertDialog.Builder, used for
 * the app's confirmation and input dialogs (Delete subject, Clear chat,
 * New subject). One shared layout (dialog_styled.xml) with two visual
 * modes:
 *
 *   - Confirm mode: icon + title + message + two STACKED buttons
 *     (used for Delete subject, Clear chat)
 *   - Input mode: icon + title + EditText + two SIDE-BY-SIDE buttons
 *     (used for New subject)
 *
 * Usage (confirm/destructive):
 *     StyledDialog.confirmDanger(context, R.drawable.ic_sheet_delete,
 *             "Delete subject?", "This will delete...",
 *             "Delete", "Cancel", () -> viewModel.deleteSubject(subject));
 *
 * Usage (input):
 *     StyledDialog.input(context, "New subject", "e.g. Biology",
 *             "Add", "Cancel", name -> viewModel.addSubject(name));
 */
public class StyledDialog {

    public interface OnConfirm {
        void run();
    }

    public interface OnInputConfirm {
        void run(String text);
    }

    /**
     * Confirm-mode dialog with a danger-red icon circle and danger-red
     * primary button. Used for destructive actions (delete, clear).
     */
    public static void confirmDanger(Context context, int iconRes, String title, String message,
                                     String primaryLabel, String secondaryLabel, OnConfirm onConfirm) {
        showConfirm(context, iconRes, R.drawable.bg_dialog_icon_circle_danger,
                R.drawable.bg_dialog_button_primary_danger, title, message,
                primaryLabel, secondaryLabel, onConfirm);
    }

    /**
     * Confirm-mode dialog with a neutral purple icon circle and purple
     * primary button. Used for non-destructive confirmations.
     */
    public static void confirmNeutral(Context context, int iconRes, String title, String message,
                                      String primaryLabel, String secondaryLabel, OnConfirm onConfirm) {
        showConfirm(context, iconRes, R.drawable.bg_dialog_icon_circle_neutral,
                R.drawable.bg_dialog_button_primary, title, message,
                primaryLabel, secondaryLabel, onConfirm);
    }

    private static void showConfirm(Context context, int iconRes, int iconBgRes, int primaryButtonBgRes,
                                    String title, String message, String primaryLabel,
                                    String secondaryLabel, OnConfirm onConfirm) {
        Dialog dialog = createBaseDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_styled, null);
        dialog.setContentView(view);

        ImageView icon = view.findViewById(R.id.dialogIcon);
        icon.setImageResource(iconRes);
        icon.setBackgroundResource(iconBgRes);

        TextView titleView = view.findViewById(R.id.dialogTitle);
        TextView messageView = view.findViewById(R.id.dialogMessage);
        titleView.setText(title);
        messageView.setText(message);
        messageView.setVisibility(View.VISIBLE);

        LinearLayout stackedButtons = view.findViewById(R.id.dialogButtonsStacked);
        stackedButtons.setVisibility(View.VISIBLE);

        TextView primaryButton = view.findViewById(R.id.dialogPrimaryButtonStacked);
        TextView secondaryButton = view.findViewById(R.id.dialogSecondaryButtonStacked);
        primaryButton.setText(primaryLabel);
        primaryButton.setBackgroundResource(primaryButtonBgRes);
        secondaryButton.setText(secondaryLabel);

        primaryButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (onConfirm != null) {
                onConfirm.run();
            }
        });
        secondaryButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        sizeDialogWindow(dialog, context);
    }

    /**
     * Input-mode dialog with the default folder-plus icon, an EditText,
     * and two side-by-side buttons (Cancel, Add). Kept for existing call
     * sites (e.g. New subject) that don't need a custom icon or prefill.
     */
    public static void input(Context context, String title, String hint,
                             String primaryLabel, String secondaryLabel, OnInputConfirm onConfirm) {
        input(context, R.drawable.ic_dialog_folder_plus, title, hint, null,
                primaryLabel, secondaryLabel, onConfirm);
    }

    /**
     * Input-mode dialog with a custom icon and an optional prefilled value
     * (e.g. the subject's current name, for renaming). Pass null for
     * prefillValue to leave the field empty.
     */
    public static void input(Context context, int iconRes, String title, String hint, String prefillValue,
                             String primaryLabel, String secondaryLabel, OnInputConfirm onConfirm) {
        Dialog dialog = createBaseDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_styled, null);
        dialog.setContentView(view);

        ImageView icon = view.findViewById(R.id.dialogIcon);
        icon.setImageResource(iconRes);
        icon.setBackgroundResource(R.drawable.bg_dialog_icon_circle_neutral);

        TextView titleView = view.findViewById(R.id.dialogTitle);
        titleView.setText(title);

        EditText input = view.findViewById(R.id.dialogInput);
        input.setHint(hint);
        input.setVisibility(View.VISIBLE);
        if (prefillValue != null) {
            input.setText(prefillValue);
            input.setSelection(prefillValue.length());
        }

        LinearLayout sideButtons = view.findViewById(R.id.dialogButtonsSideBySide);
        sideButtons.setVisibility(View.VISIBLE);

        TextView primaryButton = view.findViewById(R.id.dialogPrimaryButtonSide);
        TextView secondaryButton = view.findViewById(R.id.dialogSecondaryButtonSide);
        primaryButton.setText(primaryLabel);
        secondaryButton.setText(secondaryLabel);

        primaryButton.setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            dialog.dismiss();
            if (onConfirm != null) {
                onConfirm.run(text);
            }
        });
        secondaryButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        sizeDialogWindow(dialog, context);
    }

    private static Dialog createBaseDialog(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        return dialog;
    }

    /**
     * Plain Dialog windows don't reliably size themselves to wrap their
     * content at a readable width — without this, the card can collapse
     * to a much narrower width than the 300dp set on the root layout,
     * causing text to wrap into unreadable fragments. Called after
     * dialog.show() since window attributes can only be set on a shown
     * dialog's window.
     */
    private static void sizeDialogWindow(Dialog dialog, Context context) {
        if (dialog.getWindow() == null) {
            return;
        }
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int desiredWidth = (int) (screenWidth * 0.88);
        dialog.getWindow().setLayout(desiredWidth, android.view.WindowManager.LayoutParams.WRAP_CONTENT);
    }
}