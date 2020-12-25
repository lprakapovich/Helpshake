package com.application.helpshake.view.helpseeker;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.R;
import com.application.helpshake.databinding.ActivityEditHelpseekerProfileBinding;
import com.application.helpshake.model.BaseUser;
import com.application.helpshake.model.UserClient;
import com.application.helpshake.util.DialogBuilder;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.application.helpshake.Constants.REQUEST_IMAGE_CAPTURE;

public class EditProfileHelpSeekerActivity extends AppCompatActivity {

    private ActivityEditHelpseekerProfileBinding mBinding;
    private CollectionReference mUsersCollection;
    private BaseUser mCurrentUser;
    private String phoneNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_edit_helpseeker_profile);

        FirebaseFirestore mDb = FirebaseFirestore.getInstance();
        mUsersCollection = mDb.collection("BaseUsers");

        mCurrentUser = ((UserClient)(getApplicationContext())).getCurrentUser();

        setBindings();

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
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                } catch (ActivityNotFoundException e) {
                    // display error state to the user
                }
            }
            }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mBinding.changeButton.setImageBitmap(imageBitmap);
        }
    }

    private void readUserInput() {
        phoneNum = mBinding.phoneInput.getText().toString();
//        street = mBinding.streetInput.getText().toString();
//        homeNum = mBinding.homeNoInput.getText().toString();
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
    }

    private void setBindings() {
        mBinding.phoneInput.setText(mCurrentUser.getPhoneNumber());
//        mBinding.streetInput.setText(mCurrentUser.getStreet());
//        mBinding.homeNoInput.setText(mCurrentUser.getHomeNo());
    }
}