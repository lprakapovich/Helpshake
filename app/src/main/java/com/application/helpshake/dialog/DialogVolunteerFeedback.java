package com.application.helpshake.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.application.helpshake.R;
import com.application.helpshake.databinding.DialogVolunteerFeedbackBinding;

public class DialogVolunteerFeedback extends DialogFragment {

    public interface VolunteerFeedbackListener {
        void onFeedbackSubmitted(float rating);
    }

    private DialogVolunteerFeedbackBinding mBinding;
    private VolunteerFeedbackListener mListener;

    public DialogVolunteerFeedback(Context context) {
        mListener = (VolunteerFeedbackListener) context;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mBinding = DataBindingUtil.inflate(
                LayoutInflater.from(getContext()),
                R.layout.dialog_volunteer_feedback, null, false);

        builder.setView(mBinding.getRoot())
                .setTitle("Rate a volunteer")
                .setCancelable(false);
        addListenerOnButtonClick();
        return builder.create();
    }

    public void addListenerOnButtonClick() {

        mBinding.rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mListener.onFeedbackSubmitted(mBinding.ratingBar.getRating());
                dismiss();
            }
        });

        mBinding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

}
