package com.application.helpshake.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.MainActivity;
import com.application.helpshake.R;
import com.application.helpshake.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding mBinding;

    private FirebaseAuth mAuth;

    private String mEmail, mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mBinding.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readUserInput();
                mAuth.signInWithEmailAndPassword(mEmail, mPassword)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (isEmailVerified()) {
                            startActivity(new Intent(
                                    LoginActivity.this, MainActivity.class)
                            );
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Please verify your email first",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
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

    private void readUserInput() {
        mEmail = mBinding.email.getText().toString();
        mPassword = mBinding.password.getText().toString();
    }

    private boolean isEmailVerified() {
        return mAuth.getCurrentUser().isEmailVerified();
    }
}