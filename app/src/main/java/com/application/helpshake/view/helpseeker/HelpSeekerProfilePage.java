package com.application.helpshake.view.helpseeker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.R;
import com.application.helpshake.databinding.ActivityHelpSeekerProfilePageBinding;
import com.application.helpshake.dialog.DialogInfoRoleUpdate;
import com.application.helpshake.dialog.DialogNewHelpRequest;
import com.application.helpshake.dialog.DialogSingleResult;
import com.application.helpshake.model.enums.Role;
import com.application.helpshake.model.enums.Status;
import com.application.helpshake.model.user.BaseUser;
import com.application.helpshake.model.user.UserClient;
import com.application.helpshake.util.DialogBuilder;
import com.application.helpshake.view.auth.LoginActivity;
import com.application.helpshake.view.auth.RegisterActivity;
import com.application.helpshake.view.volunteer.VolunteerProfilePage;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class HelpSeekerProfilePage extends AppCompatActivity implements DialogSingleResult.DialogResultListener,
        DialogInfoRoleUpdate.RoleUpdateListener {

    private ActivityHelpSeekerProfilePageBinding mBinding;
    private DialogSingleResult mDialogResult;
    private DialogInfoRoleUpdate mDialog;

    private FirebaseFirestore mDb;
    private CollectionReference mUsersCollection;
    private CollectionReference mRequestsCollection;
    private BaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_help_seeker_profile_page);

        mCurrentUser = ((UserClient)(getApplicationContext())).getCurrentUser();

        mDb = FirebaseFirestore.getInstance();
        mUsersCollection = mDb.collection("BaseUsers");
        mRequestsCollection = mDb.collection("PublishedHelpRequests");

        setBindings();
    }

    private void setBindings() {
        mBinding.setNameAndSurname(mCurrentUser.getName());

        mBinding.editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(
                        HelpSeekerProfilePage.this, EditProfileHelpSeekerActivity.class));
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
                openDialogToGetConfirmation();
            }
        });
        mBinding.homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HelpSeekerProfilePage.this, HelpSeekerHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        mBinding.logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(
                        HelpSeekerProfilePage.this,
                        LoginActivity.class
                ));
            }
        });
    }

    private void becomeVolunteer() {
        Query query = mRequestsCollection
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
                    closeAllWaitingRequests();
                    updateUserRole();
                }
            }
        });
    }

    private void closeAllWaitingRequests() {
        Query query = mRequestsCollection.whereEqualTo("request.helpSeeker.uid", mCurrentUser.getUid())
                .whereEqualTo("status", Status.WaitingForApproval.toString());

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot ds : snapshots.getDocuments()) {
                    mRequestsCollection.document(ds.getId()).update("status", Status.Closed);
                }
            }
        });
    }

    private void deleteAllOpenRequests() {
        Query query = mRequestsCollection
                .whereEqualTo("request.helpSeeker.uid", mCurrentUser.getUid())
                .whereEqualTo("status", Status.Open);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot ds : snapshots.getDocuments()) {
                    mRequestsCollection.document(ds.getId()).delete();
                }
            }
        });
    }

    private void updateUserRole() {
        mUsersCollection.document(mCurrentUser.getUid())
                .update("role", Role.Volunteer)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        mDialogResult = new DialogSingleResult(
                                "Role updated",
                                "You're now a volunteer. Login once again to apply all the changes.",
                                HelpSeekerProfilePage.this);
                        mDialogResult.show(getSupportFragmentManager(), "tag");
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

    public void openDialogToGetConfirmation() {
        mDialog = new DialogInfoRoleUpdate(mCurrentUser);
        mDialog.show(getSupportFragmentManager(), getString(R.string.tag));
    }

    @Override
    public void onResult() {
        startActivity(new Intent(HelpSeekerProfilePage.this, LoginActivity.class));
    }

    @Override
    public void onConfirm() {
        mDialog.dismiss();
        becomeVolunteer();
    }

    @Override
    public void onCancel() {
        mDialog.dismiss();
    }
}