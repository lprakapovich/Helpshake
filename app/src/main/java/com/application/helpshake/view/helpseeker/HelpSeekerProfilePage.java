package com.application.helpshake.view.helpseeker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.R;
import com.application.helpshake.databinding.ActivityHelpSeekerProfilePageBinding;
import com.application.helpshake.model.enums.Role;
import com.application.helpshake.model.enums.Status;
import com.application.helpshake.model.BaseUser;
import com.application.helpshake.model.UserClient;
import com.application.helpshake.util.DialogBuilder;
import com.application.helpshake.view.auth.RegisterActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class HelpSeekerProfilePage extends AppCompatActivity {

    private ActivityHelpSeekerProfilePageBinding mBinding;
    private FirebaseFirestore mDb;
    private CollectionReference mUsersCollection;
    private BaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_help_seeker_profile_page);

        mCurrentUser = ((UserClient)(getApplicationContext())).getCurrentUser();

        mDb = FirebaseFirestore.getInstance();
        mUsersCollection = mDb.collection("BaseUsers");

        mBinding.setNameAndSurname(mCurrentUser.getName());

        mBinding.editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(
                        HelpSeekerProfilePage.this,
                        EditProfileHelpSeekerActivity.class
                ));
            }
        });

        mBinding.deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount();
            }
        });

        mBinding.becomeVolunteerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                becomeVolunteer();
            }
        });
    }

    private void becomeVolunteer() {
        Query query = mDb.collection("PublishedHelpRequests")
                .whereEqualTo("request.helpSeeker.uid", mCurrentUser.getUid())
                .whereEqualTo("status", Status.InProgress.toString());

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                if (!snapshots.getDocuments().isEmpty()) {
                    DialogBuilder.showMessageDialog(
                            getSupportFragmentManager(),
                            "Error",
                            "You change your role settings, " +
                                    "unless all your help offers are completed. "
                    );
                } else {
                    deleteAllOpenRequests();
                    updateUserRole();
                }
            }
        });
    }

    private void updateUserRole() {
        mDb.collection("BaseUsers").document(mCurrentUser.getUid())
                .update("role", Role.Volunteer)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DialogBuilder.showMessageDialog(
                                getSupportFragmentManager(),
                                "Role was changed",
                                "Please, login once again to complete all the changes."
                        );

                        // go to login page on a dialog closed
                    }
                });
    }

    private void deleteAllOpenRequests() {
        Query query = mDb.collection("PublishedHelpRequests")
                .whereEqualTo("request.helpSeeker.uid", mCurrentUser.getUid())
                .whereEqualTo("status", Status.Open);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot ds : snapshots.getDocuments()) {
                    mDb.collection("PublishedHelpRequests").document(ds.getId()).delete();
                }
            }
        });
    }

    private void deleteAccount() {
        Query query = mUsersCollection
                .whereEqualTo("email", mCurrentUser.getEmail());

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot snapshot : snapshots.getDocuments()) {
                    mUsersCollection.document(snapshot.getId()).delete();
                }
                FirebaseAuth.getInstance().getCurrentUser().delete();
                openInformationDialog();
                startActivity(new Intent(HelpSeekerProfilePage.this, RegisterActivity.class));
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