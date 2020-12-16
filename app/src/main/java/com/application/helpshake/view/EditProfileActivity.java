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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class EditProfileActivity extends AppCompatActivity {

    ActivityEditProfileBinding mBinding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;
    FirebaseUser mUser;
    private CollectionReference mUsersCollection;
    private User mCurrentUser;

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
        mUsersCollection = mDb.collection("users");

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
        Query query = mUsersCollection
                .whereEqualTo("email", mAuth.getCurrentUser().getEmail());

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot snapshot : snapshots.getDocuments()) {

                    mCurrentUser = snapshot.toObject(User.class);
                    mCurrentUser.setPhoneNum(phoneNum);
                    mCurrentUser.setStreet(street);
                    mCurrentUser.setHomeNo(homeNum);
                    mUsersCollection.document(snapshot.getId()).set(mCurrentUser);
                }
                DialogBuilder.showMessageDialog(
                        getSupportFragmentManager(),
                        "Information updated",
                        "Thanks for providing information"
                );
            }
        });
    }
}