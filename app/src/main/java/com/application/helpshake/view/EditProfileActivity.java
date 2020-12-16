package com.application.helpshake.view;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import java.io.FileNotFoundException;
import java.io.IOException;

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

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
        mUser = mAuth.getCurrentUser();
        mUsersCollection = mDb.collection("users");

        queryDataAboutUser();

        mBinding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readUserInput();
                saveInformationToDatabase();
            }
        });

        mBinding.changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                } catch (ActivityNotFoundException e) {
                    // display error state to the user
                }
            }
            }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mBinding.changeButton.setImageBitmap(imageBitmap);
        }
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

    public void queryDataAboutUser() {
        Query query = mUsersCollection
                .whereEqualTo("email", mUser.getEmail());
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot snapshot : snapshots.getDocuments()) {
                    mCurrentUser = snapshot.toObject(User.class);
                    mBinding.phoneInput.setText(mCurrentUser.getPhoneNum());
                    mBinding.streetInput.setText(mCurrentUser.getStreet());
                    mBinding.homeNoInput.setText(mCurrentUser.getHomeNo());
                }
            }
        }
        );
    }
}