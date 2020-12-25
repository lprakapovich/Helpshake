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
import com.application.helpshake.databinding.DialogRequestDetailsBinding;
import com.application.helpshake.model.PublishedHelpRequest;

import static com.application.helpshake.model.enums.HelpCategory.DogWalking;
import static com.application.helpshake.model.enums.HelpCategory.Drugstore;
import static com.application.helpshake.model.enums.HelpCategory.Grocery;

public class DialogRequestDetails extends DialogFragment {

    public interface RequestSubmittedListener {
        void OnHelpOffered();
        void OnDialogClosed();
    }

    DialogRequestDetailsBinding mBinding;
    RequestSubmittedListener mListener;
    PublishedHelpRequest helpRequest;

    public DialogRequestDetails (PublishedHelpRequest helpRequest) {
        this.helpRequest = helpRequest;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        mBinding = DataBindingUtil.inflate(
                LayoutInflater.from(getContext()),
                R.layout.dialog_request_details, null, false);

        builder.setView(mBinding.getRoot())
                .setTitle(R.string.request_details)
                .setCancelable(false);

        mBinding.commentText.setText(helpRequest.getRequest().getHelpRequest().getDescription());

        mBinding.nameAndSurnameText.setText
                (String.format("%s %s", helpRequest.getRequest().getHelpSeeker().getName(),
                        helpRequest.getRequest().getHelpSeeker().getName()));

        if (!helpRequest.getRequest().getHelpRequest().getCategoryList().contains(Grocery))
            mBinding.shopImage.setVisibility(View.GONE);
        if (!helpRequest.getRequest().getHelpRequest().getCategoryList().contains(DogWalking))
            mBinding.shopImage.setVisibility(View.GONE);
        if (!helpRequest.getRequest().getHelpRequest().getCategoryList().contains(Drugstore))
            mBinding.shopImage.setVisibility(View.GONE);

        mBinding.offerHelpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.OnHelpOffered();
            }
        });

        mBinding.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.OnDialogClosed();
            }
        });

        return builder.create();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (DialogRequestDetails.RequestSubmittedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(requireActivity().toString()
                    + "must implement OptionSelectedListener");
        }
    }
}