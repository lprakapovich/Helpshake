package com.application.helpshake.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
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
import com.application.helpshake.model.request.PublishedHelpRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static com.application.helpshake.model.enums.HelpCategory.DogWalking;
import static com.application.helpshake.model.enums.HelpCategory.Drugstore;
import static com.application.helpshake.model.enums.HelpCategory.Grocery;
import static com.application.helpshake.model.enums.HelpCategory.Other;

public class DialogRequestDetails extends DialogFragment {

    public interface RequestSubmittedListener {
        void onHelpOffered();
        void onDialogClosed();
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

        mBinding.nameAndSurnameText.setText
                (helpRequest.getRequest().getHelpSeeker().getFullName());

        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("profileImages/" + helpRequest.getRequest().getHelpSeeker().getUid() + ".jpeg");
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getContext()).load(uri)
                        .fitCenter().into(mBinding.imageView2);
            }
        });

        // DISTANCE TO DO
        //mBinding.distanceText.setText("");

        // map icon should move us to the Google Maps
        mBinding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mBinding.requestTitle.setText(helpRequest.getRequest().getHelpRequest().getTitle());

        mBinding.commentText.setText(helpRequest.getRequest().getHelpRequest().getDescription());

        if (!helpRequest.getRequest().getHelpRequest().getCategoryList().contains(Grocery))
            mBinding.grocery.setAlpha((float) 0.25);
        if (!helpRequest.getRequest().getHelpRequest().getCategoryList().contains(DogWalking))
            mBinding.dogwalking.setAlpha((float) 0.25);
        if (!helpRequest.getRequest().getHelpRequest().getCategoryList().contains(Drugstore))
            mBinding.drugstore.setAlpha((float) 0.25);
        if (!helpRequest.getRequest().getHelpRequest().getCategoryList().contains(Other))
            mBinding.other.setAlpha((float) 0.25);

        mBinding.offerHelpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onHelpOffered();
            }
        });

        mBinding.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDialogClosed();
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