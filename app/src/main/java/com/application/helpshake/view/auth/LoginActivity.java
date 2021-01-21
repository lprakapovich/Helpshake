package com.application.helpshake.view.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.Constants;
import com.application.helpshake.R;
import com.application.helpshake.databinding.ActivityLoginBinding;
import com.application.helpshake.helper.RedirectManager;
import com.application.helpshake.model.enums.Role;
import com.application.helpshake.model.user.BaseUser;
import com.application.helpshake.model.user.UserClient;
import com.application.helpshake.util.DialogBuilder;
import com.application.helpshake.view.helpseeker.HelpSeekerHomeActivity;
import com.application.helpshake.view.volunteer.VolunteerHomeActivity;
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
    private CollectionReference mBaseUsersCollection;

    private BaseUser mCurrentUser;

    private String mEmail;
    private String mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore mDb = FirebaseFirestore.getInstance();
        mBaseUsersCollection = mDb.collection("BaseUsers");

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
//        if (isEmailVerified()) {
             setContextUser();
//        } else {
//            DialogBuilder.showMessageDialog(getSupportFragmentManager(),
//                    getString(R.string.email_verification),
//                    getString(R.string.email_verification_pending));
//        }
    }

    private void setContextUser() {
        Query query = mBaseUsersCollection.whereEqualTo("email", mEmail).limit(Constants.SINGLE_RESULT);
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot ds : snapshots.getDocuments()) {
                    mCurrentUser = ds.toObject(BaseUser.class);
                    ((UserClient) (getApplicationContext())).setCurrentUser(mCurrentUser);
                }
                openHomePage();
            }
        });
    }

    private void openHomePage() {
        startActivity(new Intent(LoginActivity.this, RedirectManager.redirectTo(mCurrentUser)));
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