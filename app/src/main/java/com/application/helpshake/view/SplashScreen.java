package com.application.helpshake.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.application.helpshake.Constants;
import com.application.helpshake.R;
import com.application.helpshake.helper.RedirectManager;
import com.application.helpshake.model.user.BaseUser;
import com.application.helpshake.model.user.UserClient;
import com.application.helpshake.view.auth.LoginActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class SplashScreen extends AppCompatActivity {

    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private BaseUser mCurrentUser;
    private CollectionReference mBaseUsersCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mAuth = FirebaseAuth.getInstance();
        mBaseUsersCollection = FirebaseFirestore.getInstance().collection("BaseUsers");

        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public  void  onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                if (mAuth.getCurrentUser() != null) {
                    setContextUser();
                } else {
                    startActivity(new Intent(SplashScreen.this, LoginActivity.class));
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    private void setContextUser() {
        Query query = mBaseUsersCollection.whereEqualTo("email", mAuth.getCurrentUser().getEmail()).limit(Constants.SINGLE_RESULT);
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (DocumentSnapshot ds : snapshots.getDocuments()) {
                    mCurrentUser = ds.toObject(BaseUser.class);
                    ((UserClient) (getApplicationContext())).setCurrentUser(mCurrentUser);
                    startActivity(new Intent(SplashScreen.this, RedirectManager.redirectTo(mCurrentUser)));
                }
            }
        });
    }
}