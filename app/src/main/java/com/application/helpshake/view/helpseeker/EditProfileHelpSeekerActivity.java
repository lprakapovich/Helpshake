package com.application.helpshake.view.helpseeker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.R;
import com.application.helpshake.databinding.ActivityEditHelpseekerProfileBinding;
import com.application.helpshake.model.request.PublishedHelpRequest;
import com.application.helpshake.model.user.BaseUser;
import com.application.helpshake.model.user.UserClient;
import com.application.helpshake.util.DialogBuilder;
import com.bumptech.glide.Glide;
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

import java.util.ArrayList;

import static com.application.helpshake.Constants.GALLERY_REQUEST_CODE;
import static com.application.helpshake.Constants.REQUEST_CODE_LOCATION_PERMISSION;

public class EditProfileHelpSeekerActivity extends AppCompatActivity {

    private ActivityEditHelpseekerProfileBinding mBinding;
    private CollectionReference mUsersCollection;
    private CollectionReference mPublishedRequestsCollection;
    private BaseUser mCurrentUser;
    private String phoneNum;

    ArrayList<PublishedHelpRequest> mPublishedRequests = new ArrayList<>();

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
        
        mBinding.fetchLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLocationPermissions();
            }
        });
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("LOCATION", "permissions not granted");

            ActivityCompat.requestPermissions(
                    EditProfileHelpSeekerActivity.this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION_PERMISSION
            );
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        Log.d("LOCATION", "started fetching a location");
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
        imageData = Uri.parse(ref.getDownloadUrl().toString());
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri)
                        .fitCenter().into(mBinding.changeButton);
                //Picasso.get().load(uri).into(mBinding.changeButton);
            }
        });
    }
}