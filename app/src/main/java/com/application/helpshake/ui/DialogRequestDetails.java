package com.application.helpshake.ui;

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
import com.application.helpshake.databinding.FragmentRequestDescriptionBinding;
import com.application.helpshake.model.HelpSeekerRequest;
import com.application.helpshake.model.Status;


import static com.application.helpshake.model.HelpCategory.DogWalking;
import static com.application.helpshake.model.HelpCategory.Drugstore;
import static com.application.helpshake.model.HelpCategory.Grocery;

public class DialogRequestDetails extends DialogFragment {

    public interface RequestSubmittedListener {
        void onRequestStatusChanged(Status status);

        void OnRequestCancelled();
    }

    FragmentRequestDescriptionBinding mBinding;
    DialogRequestDetails.RequestSubmittedListener mListener;
    HelpSeekerRequest helpRequest;

    public DialogRequestDetails (HelpSeekerRequest helpRequest) {
        this.helpRequest = helpRequest;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        mBinding = DataBindingUtil.inflate(
                LayoutInflater.from(getContext()),
                R.layout.fragment_request_description, null, false);

        builder.setView(mBinding.getRoot())
                .setTitle(R.string.request_details)
                .setCancelable(false);

        mBinding.commentText.setText(helpRequest.getComment());
        mBinding.nameAndSurnameText.setText
                (String.format("%s %s", helpRequest.getHelpSeekerName(), helpRequest.getHelpSeekerSurname()));
        if (!helpRequest.getHelpCategories().contains(Grocery))
            mBinding.shopImage.setVisibility(View.GONE);
        if (!helpRequest.getHelpCategories().contains(DogWalking))
            mBinding.shopImage.setVisibility(View.GONE);
        if (!helpRequest.getHelpCategories().contains(Drugstore))
            mBinding.shopImage.setVisibility(View.GONE);

        mBinding.offerHelpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeRequestStatus();
            }
        });

        mBinding.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.OnRequestCancelled();
            }
        });

        return builder.create();
    }

    public void changeRequestStatus() {
        mListener.onRequestStatusChanged(
                Status.WaitingForApproval
        );
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