package com.application.helpshake.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import com.application.helpshake.R;
import com.application.helpshake.databinding.ActivityEditVolunteerProfileBinding;
import com.application.helpshake.helper.DialogBuilder;
import com.application.helpshake.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class EditVolunteerProfileActivity extends AppCompatActivity {

    ActivityEditVolunteerProfileBinding mBinding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;
    FirebaseUser mUser;
    private CollectionReference mUsersCollection;
    private User mCurrentUser;

    private String phoneNum;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_edit_volunteer_profile);

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
        mUser = mAuth.getCurrentUser();
        mUsersCollection = mDb.collection("users");

        queryDataAboutUser();

        mBinding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readUserInput();
                saveInformationToDatabase();
            }
        });

        mBinding.changeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                } catch (ActivityNotFoundException e) {
                    // display error state to the user
                }
            }
        });

        mBinding.changePreferances.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(
                        EditVolunteerProfileActivity.this, SettingsPopUp.class
                ));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mBinding.changeImage.setImageBitmap(imageBitmap);
        }
    }

    private void readUserInput() {
        phoneNum = mBinding.volunteerPhoneInput.getText().toString();
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
                    mBinding.volunteerPhoneInput.setText(mCurrentUser.getPhoneNum());
                }
            }
        }
        );
    }
}