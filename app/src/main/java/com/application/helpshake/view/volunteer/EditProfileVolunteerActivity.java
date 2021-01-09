package com.application.helpshake.view.volunteer;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.R;
import com.application.helpshake.databinding.ActivityEditVolunteerProfileBinding;
import com.application.helpshake.model.enums.Status;
import com.application.helpshake.model.request.PublishedHelpRequest;
import com.application.helpshake.model.user.BaseUser;
import com.application.helpshake.model.user.UserClient;
import com.application.helpshake.util.DialogBuilder;
import com.application.helpshake.view.others.SettingsPopUp;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;

import static com.application.helpshake.Constants.REQUEST_IMAGE_CAPTURE;

public class EditProfileVolunteerActivity extends AppCompatActivity {

    private ActivityEditVolunteerProfileBinding mBinding;
    private CollectionReference mUsersCollection;
    private CollectionReference mPublishedRequestsCollection;
    private BaseUser mCurrentUser;
    private String phoneNum;

    ArrayList<PublishedHelpRequest> mPublishedRequests = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_edit_volunteer_profile);

        FirebaseFirestore mDb = FirebaseFirestore.getInstance();
        mUsersCollection = mDb.collection("BaseUsers");
        mPublishedRequestsCollection = mDb.collection("PublishedHelpRequests");

        mCurrentUser = ((UserClient)(getApplicationContext())).getCurrentUser();

        setPhoneNumber();

        setBindings();
    }

    private void setBindings() {
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
                        EditProfileVolunteerActivity.this, SettingsPopUp.class
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
    }

    private void findRequestsToUpdatePhoneNum() {
        Query query = mPublishedRequestsCollection
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
                    mPublishedRequests.add(ds.toObject(PublishedHelpRequest.class));
                }
                updatePhoneNumber();
            }
        });
    }

    private void updatePhoneNumber() {
        for (PublishedHelpRequest request : mPublishedRequests) {
            mPublishedRequestsCollection.document(request.getUid()).update("volunteer.phoneNumber", phoneNum);
        }
    }

    public void setPhoneNumber() {
        mBinding.volunteerPhoneInput.setText(mCurrentUser.getPhoneNumber());
    }
}