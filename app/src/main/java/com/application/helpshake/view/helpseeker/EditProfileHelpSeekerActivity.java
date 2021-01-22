package com.application.helpshake.view.helpseeker;

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
import com.application.helpshake.databinding.ActivityEditHelpseekerProfileBinding;
import com.application.helpshake.model.enums.Status;
import com.application.helpshake.model.request.PublishedHelpRequest;
import com.application.helpshake.model.user.BaseUser;
import com.application.helpshake.model.user.UserClient;
import com.application.helpshake.util.DialogBuilder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;

import static com.application.helpshake.Constants.REQUEST_IMAGE_CAPTURE;

public class EditProfileHelpSeekerActivity extends AppCompatActivity {

    private ActivityEditHelpseekerProfileBinding mBinding;
    private CollectionReference mUsersCollection;
    private CollectionReference mPublishedRequestsCollection;
    private BaseUser mCurrentUser;
    private String phoneNum;

    ArrayList<PublishedHelpRequest> mPublishedRequests = new ArrayList<>();

    private static final int GALLERY_REQUEST_CODE = 123;
    Uri imageData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_edit_helpseeker_profile);

        FirebaseFirestore mDb = FirebaseFirestore.getInstance();
        mUsersCollection = mDb.collection("BaseUsers");
        mPublishedRequestsCollection = mDb.collection("PublishedHelpRequests");

        mCurrentUser = ((UserClient)(getApplicationContext())).getCurrentUser();
        mBinding.nameHelpSeeker.setText(mCurrentUser.getFullName());

        setImageProfile();
        setPhoneNumber();
        setBindings();
    }


    private void setBindings() {
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
                Intent intent = new Intent ();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(Intent.createChooser(intent, "Pick an image"), GALLERY_REQUEST_CODE);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageData = data.getData();
            mBinding.changeButton.setImageURI(imageData);

        }
    }

    // save to Firebase storage
    private void handleUpload(Uri uri) {

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
        handleUpload(imageData);
    }

    private void findRequestsToUpdatePhoneNum() {
        Query query = mPublishedRequestsCollection
                .whereEqualTo("request.helpSeeker.uid", mCurrentUser.getUid());

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot ds : snapshots.getDocuments()) {
                    mPublishedRequests.add(ds.toObject(PublishedHelpRequest.class));
                }
                updatePhoneNumber();
            }
        });
    }

    private void updatePhoneNumber() {
        for (PublishedHelpRequest request : mPublishedRequests) {
            mPublishedRequestsCollection.document(request.getUid()).update("request.helpSeeker.phoneNumber", phoneNum);
        }
    }

    public void setPhoneNumber() {
        mBinding.phoneInput.setText(mCurrentUser.getPhoneNumber());
    }

    public void setImageProfile() {
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("profileImages/" + mCurrentUser.getUid() + ".jpeg");

        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(mBinding.changeButton);
            }
        });
    }
}