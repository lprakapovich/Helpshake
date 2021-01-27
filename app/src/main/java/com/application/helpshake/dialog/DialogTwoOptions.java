package com.application.helpshake.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import com.application.helpshake.R;

public class DialogTwoOptions extends DialogFragment {

    private String mTitle;
    private String mMessage;
    private String mYesButton;
    private String mNoButton;
    private DialogResultListener mListener;

    public DialogTwoOptions(String title, String message, String yesButton, String noButton, Context context) {
        mTitle = title;
        mMessage = message;
        mYesButton = yesButton;
        mNoButton = noButton;
        mListener = (DialogResultListener) context;
    }

    public interface DialogResultListener {
        void onResult();
        void onCancel();
    }

    /**
     * We handle just two results, e.g. pressing an "ok" button
     */

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false)
                .setTitle(mTitle)
                .setMessage(mMessage);

        builder.setPositiveButton(mYesButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onResult();
                    }
                });

        builder.setNegativeButton(mNoButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onCancel();
            }
        });

        return builder.create();
    }


}
