package com.application.helpshake.view;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.R;
import com.application.helpshake.databinding.ActivityEditProfileBinding;
import com.application.helpshake.databinding.ActivityRegisterBinding;
import com.application.helpshake.helper.DialogBuilder;
import com.application.helpshake.model.Status;
import com.application.helpshake.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileActivity extends AppCompatActivity {

    ActivityEditProfileBinding mBinding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;
    FirebaseUser mUser;

    private String phoneNum;
    private String street;
    private String homeNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
        mUser = mAuth.getCurrentUser();

        mBinding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readUserInput();
                saveInformationToDatabase();
            }
        });
    }

    private void readUserInput() {
        phoneNum = mBinding.phoneInput.getText().toString();
        street = mBinding.streetInput.getText().toString();
        homeNum = mBinding.homeNoInput.getText().toString();
    }

    private void saveInformationToDatabase() {
        mDb.collection("users").document(mUser.getUid()).update("phoneNum", phoneNum,
                "street", street, "homeNo", homeNum)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DialogBuilder.showMessageDialog(
                                getSupportFragmentManager(),
                                "Information updated",
                                "Thanks for providing information"
                        );

                    }
                });
    }
}