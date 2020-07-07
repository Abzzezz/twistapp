/*
 * Copyright (c) 2020.
 * The code used in this project is entirely owned by Roman P.
 * Code snippets / templates / etc. are mentioned and credited.
 */

package net.bplaced.abzzezz.twistapp.util.misc;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.EditText;

/**
 * Create simple input dialogs
 */
public class InputDialogBuilder {

    private InputDialogListener dialogListener;
    private EditText editText;

    public InputDialogBuilder(InputDialogListener listener) {
        this.dialogListener = listener;
    }

    public void showInput(String title, String text, Context context) {
        this.editText = new EditText(context);
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton("Enter", (dialogInterface, i) -> dialogListener.onDialogInput(editText.getText().toString())).setNegativeButton("Cancel", (dialogInterface, i) -> dialogListener.onDialogDenied()).setView(editText).show();
    }

    public void setDialogListener(InputDialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }

    public interface InputDialogListener {
        void onDialogInput(String text);

        void onDialogDenied();
    }
}
