package com.application.helpshake.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DialogSelect extends DialogFragment {

    public interface OptionSelectedListener {
        void onOptionSelected(DialogFragment dialog, int option);
    }

    private String mTitle;
    private String[] mOptions;
    private OptionSelectedListener mListener;

    public DialogSelect(String title, String[] options) {
        mTitle = title;
        mOptions = options;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false)
                .setTitle(mTitle)
                .setItems(mOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onOptionSelected(DialogSelect.this, which);
                    }
                });

        return builder.create();
    }
}
