package com.application.helpshake.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.R;
import com.application.helpshake.databinding.ActivityHelpSeekerProfilePageBinding;
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

public class HelpSeekerProfilePage extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    User user;
    FirebaseFirestore mDb;
    CollectionReference mRequestsCollection;
    CollectionReference mUsersCollection;
    private ActivityHelpSeekerProfilePageBinding helpSeekerProfileBinding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Binding tutorial: https://codelabs.developers.google.com/codelabs/android-databinding#4
        //https://www.raywenderlich.com/7711166-data-binding-in-android-getting-started#toc-anchor-014
        helpSeekerProfileBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_help_seeker_profile_page);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        String name = mUser.getDisplayName();
        helpSeekerProfileBinding.setNameAndSurname(name);

        mDb = FirebaseFirestore.getInstance();
        mRequestsCollection = mDb.collection(getString(R.string.collectionHelpSeekerRequests));
        mUsersCollection = mDb.collection("users");

        helpSeekerProfileBinding.editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(
                        HelpSeekerProfilePage.this, EditProfileActivity.class
                ));
            }
        });

        helpSeekerProfileBinding.deleteAccountButton.setOnClickListener(new View.OnClickListener() {
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

                        openInformationDialog();
                        startActivity(new Intent(HelpSeekerProfilePage.this, RegisterActivity.class));

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