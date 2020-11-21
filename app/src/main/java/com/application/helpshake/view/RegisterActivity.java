package com.application.helpshake.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.application.helpshake.databinding.ActivityRegisterBinding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.application.helpshake.R;
import com.application.helpshake.model.Role;
import com.application.helpshake.model.User;
import com.application.helpshake.ui.DialogSelect;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class RegisterActivity extends AppCompatActivity
        implements DialogSelect.OptionSelectedListener {

    private ActivityRegisterBinding mBinding;
    private FirebaseAuth mAuth;

    private String mName,
            mSurname,
            mEmail,
            mPassword,
            mRepeatPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_register);

        mBinding.register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readUserInput();
                if (passwordsMatch() && allFilled()) {
                    selectUserRole();
                    //register();
                }
            }
        });

        mBinding.redirectToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(
                        RegisterActivity.this, LoginActivity.class
                ));
            }
        });
    }

    private void selectUserRole() {
        String[] options = new String[] {
                getString(R.string.helpseeker_role),
                getString(R.string.volunteer_role)
        };

        DialogSelect dialog = new DialogSelect(
                getString(R.string.select_role_title),
                options
        );
        dialog.show(getSupportFragmentManager(), getString(R.string.tag));
    }


    private void readUserInput() {
        mEmail = mBinding.email.getText().toString();
        mPassword = mBinding.password.getText().toString();
        mRepeatPassword = mBinding.passwordRepeated.getText().toString();
    }

    private void register() {
        mAuth.createUserWithEmailAndPassword(mEmail, mPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sendEmailVerificationLink();
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthUserCollisionException e) {
                                // such user email already registered
                            } catch (Exception e) {
                                // something else went wrong
                            }
                        }
                    }
                });
    }

    private void sendEmailVerificationLink() {
        mAuth.getCurrentUser().sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(),
                            "Please check your email",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private boolean passwordsMatch() {
        return !mPassword.isEmpty()
                && !mRepeatPassword.isEmpty()
                && mPassword.equals(mRepeatPassword);
    }

    private boolean allFilled() {
        return !mEmail.isEmpty();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onOptionSelected(DialogFragment dialog, int option) {
        Role role;
        switch (option) {
            case 0:
                setUserRole(Role.HelpSeeker);
                break;
            case 1:
                setUserRole(Role.Volunteer);
                break;
        }
    }

    private void setUserRole(Role role) {
        User user = new User();
    }
}