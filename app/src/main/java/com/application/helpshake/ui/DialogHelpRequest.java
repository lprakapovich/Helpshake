package com.application.helpshake.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.application.helpshake.R;
import com.application.helpshake.databinding.FragmentAddRequestBinding;
import com.application.helpshake.helper.DialogBuilder;
import com.application.helpshake.model.HelpCategory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DialogHelpRequest extends DialogFragment {

    public interface RequestSubmittedListener {
        void onRequestSubmitted(String comment, List<HelpCategory> categories);
        void OnRequestCancelled();
    }

    FragmentAddRequestBinding mBinding;
    RequestSubmittedListener mListener;
    Set<HelpCategory> mCategories = new HashSet<>();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        mBinding = DataBindingUtil.inflate(
                LayoutInflater.from(getContext()),
                R.layout.fragment_add_request, null, false);

        setOnCheckBoxSelected();
        builder.setView(mBinding.getRoot())
                .setTitle("Adding a help request")
                .setCancelable(false);

        mBinding.addRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCategoryChosen()) {
                    submitRequest();
                } else {
                    DialogBuilder.showMessageDialog(
                            getParentFragmentManager(),
                            "Not allowed",
                            "Request must contain at least one category");
                }
            }
        });

        mBinding.cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.OnRequestCancelled();
            }
        });

        return builder.create();
    }

    private boolean isCategoryChosen() {
        return !mCategories.isEmpty();
    }

    private void submitRequest() {
        mListener.onRequestSubmitted(
                mBinding.comment.getText().toString(),
                new ArrayList<>(mCategories)
        );
    }

    private void setOnCheckBoxSelected() {
        setOnClickListener(mBinding.grocery, HelpCategory.Grocery);
        setOnClickListener(mBinding.dogwalking, HelpCategory.DogWalking);
        setOnClickListener(mBinding.drugstore, HelpCategory.Drugstore);
        setOnClickListener(mBinding.other, HelpCategory.Other);
    }

    private void setOnClickListener(CheckBox checkBox, final HelpCategory category) {
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mCategories.add(category);
                } else {
                    mCategories.remove(category);
                }
            }
        });
    }

    // sets calling activity as a listener
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (RequestSubmittedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(requireActivity().toString()
                    + "must implement OptionSelectedListener");
        }
    }
}
