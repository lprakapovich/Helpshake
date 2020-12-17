package com.application.helpshake.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.R;
import com.application.helpshake.databinding.ActivityVolunteerProfilePageBinding;
import com.application.helpshake.helper.DialogBuilder;
import com.application.helpshake.model.User;
import com.application.helpshake.view.volunteer.EditVolunteerProfileActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class VolunteerProfilePage extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    private ActivityVolunteerProfilePageBinding volunteerProfileBinding ;

    User user;
    FirebaseFirestore mDb;
    CollectionReference mRequestsCollection;
    CollectionReference mUsersCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        volunteerProfileBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_volunteer_profile_page);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        String name = mUser.getDisplayName();
        volunteerProfileBinding.setNameAndSurname(name);

        volunteerProfileBinding.homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mDb = FirebaseFirestore.getInstance();
        mRequestsCollection = mDb.collection(getString(R.string.collectionHelpSeekerRequests));
        mUsersCollection = mDb.collection("users");

        volunteerProfileBinding.editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(
                        VolunteerProfilePage.this, EditVolunteerProfileActivity.class
                ));
            }
        });

       volunteerProfileBinding.deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Query query = mUsersCollection
                        .whereEqualTo("email", mAuth.getCurrentUser().getEmail());

                query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        for (DocumentSnapshot snapshot : snapshots.getDocuments()) {
                            mUsersCollection.document(snapshot.getId()).delete();
                        }
                        mUser.delete();
                        openInformationDialog();
                        startActivity(new Intent(VolunteerProfilePage.this, RegisterActivity.class));

                    }
                });
            }

        });
    }


    public void openInformationDialog() {
        DialogBuilder.showMessageDialog(
                getSupportFragmentManager(),
                "Your account was deleted",
                "Thanks for using our app."
        );
    }
}