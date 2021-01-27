package com.application.helpshake.view.helpseeker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.R;
import com.application.helpshake.databinding.ActivityHelpSeekerProfilePageBinding;
import com.application.helpshake.dialog.DialogInfoRoleUpdate;
import com.application.helpshake.dialog.DialogSingleResult;
import com.application.helpshake.model.enums.Role;
import com.application.helpshake.model.enums.Status;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

import static com.application.helpshake.Constants.GALLERY_REQUEST_CODE;

public class HelpSeekerProfilePage extends AppCompatActivity implements DialogSingleResult.DialogResultListener,
        DialogInfoRoleUpdate.RoleUpdateListener {

    private ActivityHelpSeekerProfilePageBinding mBinding;
    private DialogSingleResult mDialogResult;
    private DialogInfoRoleUpdate mDialog;

    private FirebaseFirestore mDb;
    private CollectionReference mUsersCollection;
    private CollectionReference mRequestsCollection;
    private BaseUser mCurrentUser;
    private Uri imageData;
    private String phoneNum;

    private ArrayList<PublishedHelpRequest> mRequests =  new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_help_seeker_profile_page);

        mCurrentUser = ((UserClient) (getApplicationContext())).getCurrentUser();

        mDb = FirebaseFirestore.getInstance();
        mUsersCollection = mDb.collection("BaseUsers");
        mRequestsCollection = mDb.collection("PublishedHelpRequests");

        //getSupportActionBar().setTitle(mCurrentUser.getName());
        setBindings();
        setPhoneNumber();
        setProfilePic();
    }

    private void setBindings() {
        mBinding.nameAndSurnameText.setText(mCurrentUser.getFullName());

        mBinding.deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //deleteAccount();
            }
        });

        mBinding.becomeVolunteerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogToGetConfirmation();
            }
        });

        mBinding.savePhoneButtonH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readUserInput();
                saveInformationToDatabase();
            }
        });

        mBinding.profilePic.setOnClickListener(new View.OnClickListener() {
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

    public void setProfilePic() {
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("profileImages/" + mCurrentUser.getUid() + ".jpeg");
        imageData = Uri.parse(ref.getDownloadUrl().toString());
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri)
                        .fitCenter().into(mBinding.profilePic);
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

    //-----------------------------------from edit-----------------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageData = data.getData();
            mBinding.profilePic.setImageURI(imageData);
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
        phoneNum = mBinding.phoneInputH.getText().toString();
    }

    private void saveInformationToDatabase() {
        mUsersCollection.document(mCurrentUser.getUid()).update(
                "phoneNumber", phoneNum)
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
                .whereEqualTo("request.helpSeeker.uid", mCurrentUser.getUid());

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
            mRequestsCollection.document(request.getUid()).update("request.helpSeeker.phoneNumber", phoneNum);
        }
    }

    public void setPhoneNumber() {
        mBinding.phoneInputH.setText(mCurrentUser.getPhoneNumber());
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

}