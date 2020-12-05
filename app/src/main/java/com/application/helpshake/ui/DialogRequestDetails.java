package com.application.helpshake.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.application.helpshake.R;
import com.application.helpshake.databinding.FragmentRequestDescriptionBinding;
import com.application.helpshake.model.HelpCategory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DialogRequestDetails extends DialogFragment {

    public interface RequestSubmittedListener {
        void onRequestSubmitted(String comment, List<HelpCategory> categories);

        void OnRequestCancelled();
    }

    FragmentRequestDescriptionBinding mBinding;
    DialogRequestDetails.RequestSubmittedListener mListener;
    Set<HelpCategory> mCategories = new HashSet<>();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        mBinding = DataBindingUtil.inflate(
                LayoutInflater.from(getContext()),
                R.layout.fragment_request_description, null, false);

        builder.setView(mBinding.getRoot())
                .setTitle("Request details")
                .setCancelable(false);

        return builder.create();
    }
}