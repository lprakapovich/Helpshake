package com.application.helpshake.view.volunteer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.R;
import com.application.helpshake.databinding.ActivityVolunteerProfilePageBinding;
import com.application.helpshake.dialog.DialogInfoRoleUpdate;
import com.application.helpshake.dialog.DialogSingleResult;
import com.application.helpshake.model.enums.Role;
import com.application.helpshake.model.enums.Status;
import com.application.helpshake.model.notification.NotificationClosedRequest;
import com.application.helpshake.model.request.PublishedHelpRequest;
import com.application.helpshake.model.user.BaseUser;
import com.application.helpshake.model.user.UserClient;
import com.application.helpshake.util.DialogBuilder;
import com.application.helpshake.view.auth.LoginActivity;
import com.application.helpshake.view.auth.RegisterActivity;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;

import static com.application.helpshake.Constants.GALLERY_REQUEST_CODE;

public class VolunteerProfilePage extends AppCompatActivity implements DialogSingleResult.DialogResultListener,
        DialogInfoRoleUpdate.RoleUpdateListener {

    private ActivityVolunteerProfilePageBinding mBinding;
    private DialogSingleResult mDialogResult;
    private DialogInfoRoleUpdate mDialog;

    FirebaseFirestore mDb;
    CollectionReference mRequestsCollection;
    CollectionReference mNotificationsCollection;
    private String phoneNum;
    CollectionReference mUsersCollection;
    BaseUser mCurrentUser;
    Uri imageData;

    ArrayList<PublishedHelpRequest> mRequests = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_volunteer_profile_page);

        mDb = FirebaseFirestore.getInstance();
        mRequestsCollection = mDb.collection("PublishedHelpRequests");
        mNotificationsCollection = mDb.collection("Notifications");
        mUsersCollection = mDb.collection("BaseUsers");
        mCurrentUser = ((UserClient) (getApplicationContext())).getCurrentUser();

      //  getSupportActionBar().setTitle(mCurrentUser.getName());
        setBindings();
        setProfilePic();
    }

    private void setBindings() {
        mBinding.setNameAndSurname(mCurrentUser.getFullName());

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

        mBinding.savePhoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readUserInput();
                saveInformationToDatabase();
            }
        });

        mBinding.volProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(Intent.createChooser(intent, "Pick an image"), GALLERY_REQUEST_CODE);
                }

            }
        });

    }


    private void becomeHelpSeeker() {
        Query query = mRequestsCollection
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

    public void setProfilePic() {
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("profileImages/" + mCurrentUser.getUid() + ".jpeg");
        imageData = Uri.parse(ref.getDownloadUrl().toString());
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri)
                        .fitCenter().into(mBinding.volProfilePic);
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

    //---------------------------------------------from edit-------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageData = data.getData();
            mBinding.volProfilePic.setImageURI(imageData);

        }
    }

    private void saveToFirebaseStorage(Uri uri) {
        String uid = mCurrentUser.getUid();
        String path = "profileImages/" + uid + ".jpeg";
        final StorageReference reference = FirebaseStorage.getInstance().getReference(path);

        reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                reference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        // Picasso.get().load(task.getResult()).into(mBinding.changeImage);
                    }
                });
            }
        });
    }

    private void readUserInput() {
        phoneNum = mBinding.phoneInput.getText().toString();
    }

    private void saveInformationToDatabase() {
        mUsersCollection.document(mCurrentUser.getUid()).update("phoneNumber", phoneNum)
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
        mCurrentUser.setPhoneNumber(phoneNum);
        findRequestsToUpdatePhoneNum();
        saveToFirebaseStorage(imageData);
    }

    private void findRequestsToUpdatePhoneNum() {
        Query query = mRequestsCollection
                .whereEqualTo("volunteer.uid", mCurrentUser.getUid())
                .whereIn("status", Arrays.asList(Status.InProgress.toString(),
                        Status.WaitingForApproval.toString(),
                        Status.Closed.toString(),
                        Status.Declined.toString(),
                        Status.Closed.toString()));

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot ds : snapshots.getDocuments()) {
                    mRequests.add(ds.toObject(PublishedHelpRequest.class));
                }
                updatePhoneNumber();
            }
        });
    }

    private void updatePhoneNumber() {
        for (PublishedHelpRequest request : mRequests) {
            mRequestsCollection.document(request.getUid()).update("volunteer.phoneNumber", phoneNum);
        }
    }

    public void setPhoneNumber() {
        mBinding.phoneInput.setText(mCurrentUser.getPhoneNumber());
    }

    public void setImageProfile() {
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("profileImages/" + mCurrentUser.getUid() + ".jpeg");
        imageData = Uri.parse(ref.getDownloadUrl().toString());
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri)
                        .fitCenter().into(mBinding.volProfilePic);
                //Picasso.get().load(uri).into(mBinding.changeImage);
            }
        });
    }

}