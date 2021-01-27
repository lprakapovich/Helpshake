package com.application.helpshake.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.application.helpshake.R;
import com.application.helpshake.databinding.DialogVolunteerFeedbackBinding;

public class DialogVolunteerFeedback extends DialogFragment {

    public interface VolunteerFeedbackListener {
        void onFeedbackSubmitted();
    }

    DialogVolunteerFeedbackBinding mBinding;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mBinding = DataBindingUtil.inflate(
                LayoutInflater.from(getContext()),
                R.layout.dialog_volunteer_feedback, null, false);

        builder.setView(mBinding.getRoot())
                .setTitle(R.string.request_details)
                .setCancelable(false);
        addListenerOnButtonClick();
        return builder.create();
    }

    public void addListenerOnButtonClick() {

        mBinding.button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String rating = String.valueOf(mBinding.ratingBar.getRating());
                Toast.makeText(getContext(), rating, Toast.LENGTH_LONG).show();
            }

        });
    }

}
