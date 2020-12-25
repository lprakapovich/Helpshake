package com.application.helpshake.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.application.helpshake.R;

public class DialogSingleResult extends DialogFragment {

    private String mTitle;
    private String mMessage;
    private DialogResultListener mListener;

    public DialogSingleResult(String title, String message, Context context) {
        mTitle = title;
        mMessage = message;
        mListener = (DialogResultListener) context;
    }

    public interface DialogResultListener {
        void onResult();
    }

    /**
     * We handle just a single result, e.g. pressing an "ok" button
     */

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false)
                .setTitle(mTitle)
                .setMessage(mMessage)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       mListener.onResult();
                    }
                });
        return builder.create();
    }
}
