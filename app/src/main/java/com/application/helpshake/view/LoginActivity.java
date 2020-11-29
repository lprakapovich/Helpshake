package com.application.helpshake.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.R;
import com.application.helpshake.databinding.ActivityLoginBinding;
import com.application.helpshake.helper.DialogBuilder;
import com.application.helpshake.model.Role;
import com.application.helpshake.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;


public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding mBinding;

    private FirebaseAuth mAuth;
    private String mUid;
    private FirebaseFirestore mDb;
    private CollectionReference mUsersCollection;


    private String mEmail, mPassword;
    private User mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
        mUsersCollection = mDb.collection("users");

        mBinding.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readUserInput();
                if (!emptyInput()) {
                    login();
                }
            }
        });

        mBinding.redirectToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(
                        LoginActivity.this, RegisterActivity.class
                ));
            }
        });
    }

    private void login() {
        mAuth.signInWithEmailAndPassword(mEmail, mPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mUid = mAuth.getCurrentUser().getUid();
                            mEmail = mAuth.getCurrentUser().getEmail();
                            validateEmail();
                        } else {
                            DialogBuilder.showMessageDialog(getSupportFragmentManager(),
                                    getString(R.string.error),
                                    getString(R.string.error_message));
                        }
                    }
                });
    }

    private void validateEmail() {

        if (isEmailVerified()) {

            updateUserDocumentUid();

        } else {
            DialogBuilder.showMessageDialog(getSupportFragmentManager(),
                    getString(R.string.email_verification),
                    getString(R.string.email_verification_pending));
        }
    }

    private void updateUserDocumentUid() {

        Query query = mUsersCollection
                .whereEqualTo("email", mEmail);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot snapshot : snapshots.getDocuments()) {

                    mCurrentUser = snapshot.toObject(User.class);

                    if (mCurrentUser.getUid().isEmpty()) {
                        assert mCurrentUser != null;
                        mCurrentUser.setUid(mUid);
                        updateDocumentData(snapshot.getId());
                    }

                    openHomePage();
                }
            }
        });
    }

    private void updateDocumentData(String documentId) {
        mUsersCollection.document(documentId).set(mCurrentUser);
    }

    private void openHomePage() {
        Class<? extends AppCompatActivity> target;
        if (mCurrentUser.getRole().equals(Role.HelpSeeker)) {
            target = HelpSeekerHomeActivity.class;
        } else {
            target = VolunteerProfilePage.class;
        }
        startActivity(new Intent(LoginActivity.this, target));
    }

    private boolean emptyInput() {
        return mEmail.isEmpty() || mPassword.isEmpty();
    }

    private void readUserInput() {
        mEmail = mBinding.email.getText().toString();
        mPassword = mBinding.password.getText().toString();
    }

    private boolean isEmailVerified() {
        return mAuth.getCurrentUser().isEmailVerified();
    }
}