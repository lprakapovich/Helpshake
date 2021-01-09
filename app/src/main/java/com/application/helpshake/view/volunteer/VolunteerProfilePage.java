package com.application.helpshake.view.volunteer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.R;
import com.application.helpshake.databinding.ActivityVolunteerProfilePageBinding;
import com.application.helpshake.dialog.DialogInfoRoleUpdate;
import com.application.helpshake.dialog.DialogSingleResult;
import com.application.helpshake.model.user.BaseUser;
import com.application.helpshake.model.notification.NotificationClosedRequest;
import com.application.helpshake.model.request.PublishedHelpRequest;
import com.application.helpshake.model.user.UserClient;
import com.application.helpshake.model.enums.Role;
import com.application.helpshake.model.enums.Status;
import com.application.helpshake.util.DialogBuilder;
import com.application.helpshake.view.auth.LoginActivity;
import com.application.helpshake.view.auth.RegisterActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class VolunteerProfilePage extends AppCompatActivity implements DialogSingleResult.DialogResultListener,
        DialogInfoRoleUpdate.RoleUpdateListener{

    private ActivityVolunteerProfilePageBinding mBinding;
    private DialogSingleResult mDialogResult;
    private DialogInfoRoleUpdate mDialog;

    FirebaseFirestore mDb;
    CollectionReference mRequestsCollection;
    CollectionReference mNotificationsCollection;

    CollectionReference mUsersCollection;
    BaseUser mCurrentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_volunteer_profile_page);

        mDb = FirebaseFirestore.getInstance();
        mRequestsCollection = mDb.collection("PublishedHelpRequests");
        mNotificationsCollection = mDb.collection("Notifications");
        mUsersCollection = mDb.collection("BaseUsers");
        mCurrentUser = ((UserClient)(getApplicationContext())).getCurrentUser();
        setBindings();
    }

    private void setBindings() {
        mBinding.setNameAndSurname(mCurrentUser.getFullName());

        mBinding.homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mBinding.editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(VolunteerProfilePage.this, EditProfileVolunteerActivity.class));
            }
        });

        mBinding.deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount();
            }
        });

        mBinding.becomeHelpSeeker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogToGetConfirmation();
            }
        });
    }

    private void becomeHelpSeeker() {
        Query query =mRequestsCollection
                .whereEqualTo("volunteer.uid", mCurrentUser.getUid())
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
                    closeAllPendingHelpOffers();
                    updateUserRole();
                }
            }
        });
    }

    private void closeAllPendingHelpOffers() {
        Query query = mRequestsCollection.whereEqualTo("volunteer.uid", mCurrentUser.getUid())
                .whereEqualTo("status", Status.WaitingForApproval.toString());

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot ds : snapshots.getDocuments()) {
                    mRequestsCollection.document(ds.getId()).update("status", Status.Closed);

                    PublishedHelpRequest r = ds.toObject(PublishedHelpRequest.class);

                    DocumentReference notificationDocument = mNotificationsCollection.document();

                    NotificationClosedRequest notification = new NotificationClosedRequest(
                            notificationDocument.getId(),
                            r.getRequest().getHelpSeeker(),
                            r.getVolunteer(),
                            "Help offer was closed",
                            "Volunteer has switched to a help seeker role, all his help offers were suspended",
                            false,
                            r.getUid()
                    );

                    mNotificationsCollection.document().set(notification);
                }
            }
        });
    }

    private void updateUserRole() {
        mDb.collection("BaseUsers").document(mCurrentUser.getUid())
                .update("role", Role.HelpSeeker)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                mDialogResult = new DialogSingleResult(
                        "Role updated",
                        "You're now a help seeker. Login once again to apply all the changes.",
                        VolunteerProfilePage.this);
                mDialogResult.show(getSupportFragmentManager(), "tag");
            }
        });
    }

    public void openDialogToGetConfirmation() {
        mDialog = new DialogInfoRoleUpdate(mCurrentUser);
        mDialog.show(getSupportFragmentManager(), getString(R.string.tag));
    }

    public void deleteAccount() {
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
                startActivity(new Intent(VolunteerProfilePage.this, RegisterActivity.class));
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

    @Override
    public void onResult() {
        startActivity(new Intent(VolunteerProfilePage.this, LoginActivity.class));
    }

    @Override
    public void onConfirm() {
        mDialog.dismiss();
        becomeHelpSeeker();
    }

    @Override
    public void onCancel() {
        mDialog.dismiss();
    }
}