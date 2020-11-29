package com.application.helpshake.view;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.R;
import com.application.helpshake.databinding.ActivityHelpSeekerHomeBinding;
import com.application.helpshake.helper.DialogBuilder;
import com.application.helpshake.model.HelpCategory;
import com.application.helpshake.model.HelpSeekerRequest;
import com.application.helpshake.model.Status;
import com.application.helpshake.ui.DialogHelpRequest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class HelpSeekerHomeActivity extends AppCompatActivity
        implements DialogHelpRequest.RequestSubmittedListener {

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mDb;

    ActivityHelpSeekerHomeBinding mBinding;
    DialogHelpRequest mDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_help_seeker_home);

        mBinding.newRequestButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openRequestDialog();
                    }
                }
        );

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDb = FirebaseFirestore.getInstance();
    }

    private void openRequestDialog() {
        mDialog = new DialogHelpRequest();
        mDialog.show(getSupportFragmentManager(), "help request");
    }

    @Override
    public void onRequestSubmitted(String comment, List<HelpCategory> categories) {
        mDialog.dismiss();
        createNewRequest(comment, categories);
    }

    @Override
    public void OnRequestCancelled() {
        mDialog.dismiss();
    }

    private void createNewRequest(String comment, List<HelpCategory> categories) {
        HelpSeekerRequest request = new HelpSeekerRequest(
                mUser.getUid(),
                categories,
                Status.Open,
                comment
        );

        mDb.collection("helpSeekerRequests").document().set(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                DialogBuilder.showMessageDialog(
                        getSupportFragmentManager(),
                        "Request is published",
                        "Thank you! Our volunteers will reach out to you."
                );
            }
        });
    }
}
