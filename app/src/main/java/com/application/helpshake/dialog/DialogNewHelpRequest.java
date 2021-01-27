package com.application.helpshake.dialog;

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
import com.application.helpshake.databinding.DialogNewHelpRequestBinding;
import com.application.helpshake.util.DialogBuilder;
import com.application.helpshake.model.enums.HelpCategory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DialogNewHelpRequest extends DialogFragment {


    public interface NewRequestListener {
        void onRequestCreated(String title, String comment, List<HelpCategory> categories);

        void onRequestCancelled();
    }

    DialogNewHelpRequestBinding mBinding;
    NewRequestListener mListener;
    Set<HelpCategory> mCategories = new HashSet<>();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        mBinding = DataBindingUtil.inflate(
                LayoutInflater.from(getContext()),
                R.layout.dialog_new_help_request, null, false);

        setOnCheckBoxSelected();
        builder.setView(mBinding.getRoot())
                .setTitle(getString(R.string.adding_request))
                .setCancelable(false);

        mBinding.addRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCategoryChosen() && isTitleProvided()) {
                    submitRequest();
                } else {
                    DialogBuilder.showMessageDialog(
                            getParentFragmentManager(),
                            getString(R.string.not_allowed),
                            getString(R.string.add_request_error));
                }
            }
        });

        mBinding.cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onRequestCancelled();
            }
        });

        setInitialInactiveCheckBoxButtonsOpacity();

        return builder.create();
    }

    private boolean isCategoryChosen() {
        return !mCategories.isEmpty();
    }

    private boolean isTitleProvided() {
        return !mBinding.title.getText().toString().isEmpty();
    }

    private void submitRequest() {
        mListener.onRequestCreated(
                mBinding.title.getText().toString(),
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
                    buttonView.setAlpha((float) 1.0);
                } else {
                    mCategories.remove(category);
                    buttonView.setAlpha((float) 0.5);
                }
            }
        });
    }

    void setInitialInactiveCheckBoxButtonsOpacity() {
        mBinding.grocery.setAlpha((float) 0.5);
        mBinding.dogwalking.setAlpha((float) 0.5);
        mBinding.drugstore.setAlpha((float) 0.5);
        mBinding.other.setAlpha((float) 0.5);
    }

    // sets calling activity as a listener
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (NewRequestListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(requireActivity().toString()
                    + "must implement OptionSelectedListener");
        }
    }
}
