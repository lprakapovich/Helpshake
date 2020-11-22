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
import com.application.helpshake.helper.DialogBuilder;
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
            startActivity(new Intent(
                    LoginActivity.this, MainActivity.class));
        } else {
            DialogBuilder.showMessageDialog(getSupportFragmentManager(),
                    getString(R.string.email_verification),
                    getString(R.string.email_verification_pending));
        }
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