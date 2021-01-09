package com.application.helpshake.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.application.helpshake.R;
import com.application.helpshake.databinding.DialogInfoAboutLostInformationBinding;

public class DialogInfoRoleUpdate extends DialogFragment {

    public interface RoleUpdateListener {
        void onConfirm();
        void onCancel();
    }

    DialogInfoAboutLostInformationBinding mBinding;
    DialogInfoRoleUpdate.RoleUpdateListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        mBinding = DataBindingUtil.inflate(
                LayoutInflater.from(getContext()),
                R.layout.dialog_info_about_lost_information, null, false);

        builder.setView(mBinding.getRoot())
                .setCancelable(false);

        mBinding.infoText.setText(getString(R.string.change_role_on_volunteer));
        mBinding.questionText.setText(getString(R.string.confirmation_question));

        mBinding.yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               mListener.onConfirm();
            }
        });

        mBinding.noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCancel();
            }
        });

        return builder.create();
    }

    // sets calling activity as a listener
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (DialogInfoRoleUpdate.RoleUpdateListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(requireActivity().toString()
                    + "must implement OptionSelectedListener");
        }
    }
}

