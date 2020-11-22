package com.application.helpshake.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.application.helpshake.databinding.ActivityRegisterBinding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.application.helpshake.R;
import com.application.helpshake.helper.DialogBuilder;
import com.application.helpshake.model.Role;
import com.application.helpshake.ui.DialogSelect;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity
        implements DialogSelect.OptionSelectedListener {

    private ActivityRegisterBinding mBinding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;

    private String mName,
            mSurname,
            mEmail,
            mPassword,
            mRepeatPassword;

    private Role mRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();

        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_register);

        mBinding.register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readUserInput();
                if (!emptyInputs() && passwordsMatch()) {
                    selectUserRole();
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

    private void readUserInput() {
        mName = mBinding.name.getText().toString();
        mSurname = mBinding.surname.getText().toString();
        mEmail = mBinding.email.getText().toString();
        mPassword = mBinding.password.getText().toString();
        mRepeatPassword = mBinding.passwordRepeated.getText().toString();
    }

    private boolean passwordsMatch() {
        return mPassword.equals(mRepeatPassword);
    }

    private boolean emptyInputs() {
        return mName.isEmpty()
                || mSurname.isEmpty()
                || mEmail.isEmpty()
                || mPassword.isEmpty()
                || mRepeatPassword.isEmpty();
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

    @Override
    public void onOptionSelected(DialogFragment dialog, int option) {
        switch (option) {
            case 0:
                mRole = Role.HelpSeeker;
                break;
            case 1:
                mRole = Role.Volunteer;
                break;
        }
        register();
    }

    private void register() {
        mAuth.createUserWithEmailAndPassword(mEmail, mPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            saveCredentialsToFireStore();
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthUserCollisionException e) {
                                DialogBuilder.showMessageDialog(getSupportFragmentManager(),
                                        getString(R.string.error),
                                        getString(R.string.email_in_use));
                            } catch (Exception e) {
                                DialogBuilder.showMessageDialog(getSupportFragmentManager(),
                                        getString(R.string.error),
                                        getString(R.string.error_message));
                            }
                        }
                    }
                });
    }

    private void saveCredentialsToFireStore() {
        DocumentReference userDocument = mDb.collection("users").document();
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", mName);
        userData.put("surname", mSurname);
        userData.put("email", mEmail);
        userData.put("role", mRole);

        userDocument.set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        sendEmailVerificationLink();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        DialogBuilder.showMessageDialog(getSupportFragmentManager(),
                                getString(R.string.error),
                                getString(R.string.error_message));
                    }
                });
    }

    private void sendEmailVerificationLink() {
        mAuth.getCurrentUser().sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            resetInputs();
                            DialogBuilder.showMessageDialog(getSupportFragmentManager(),
                                    getString(R.string.email_verification),
                                    getString(R.string.email_verification_sent)
                            );
                        }
                    }
                });
    }

    private void resetInputs() {
        mBinding.email.setText("");
        mBinding.name.setText("");
        mBinding.surname.setText("");
        mBinding.password.setText("");
        mBinding.passwordRepeated.setText("");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}