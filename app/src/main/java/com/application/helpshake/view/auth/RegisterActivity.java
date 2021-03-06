package com.application.helpshake.view.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.application.helpshake.R;
import com.application.helpshake.databinding.ActivityRegisterBinding;
import com.application.helpshake.util.DialogBuilder;
import com.application.helpshake.model.enums.Role;
import com.application.helpshake.model.user.BaseUser;
import com.application.helpshake.dialog.DialogSelect;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import org.apache.commons.lang3.StringUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


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
                if (nonEmptyInputs() && passwordsMatch()) {
                    selectUserRole();
                }
            }
        });

        mBinding.redirectToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class
                ));
            }
        });
    }

    private boolean passwordsMatch() {
        return mPassword.equals(mRepeatPassword);
    }

    private boolean nonEmptyInputs() {
        return !StringUtils.isAnyEmpty(mName, mSurname, mEmail, mPassword, mRepeatPassword);
    }

    private void selectUserRole() {
        String[] options = new String[] {
                getString(R.string.help_seeker_role),
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
                            setUserDisplayName();
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

        DocumentReference baseUserDocument = mDb.collection("BaseUsers").document();

        BaseUser baseUser = new BaseUser(
                baseUserDocument.getId(),
                mName,
                mSurname,
                mRole,
                mEmail,
                null,
                null);

        baseUserDocument.set(baseUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DialogBuilder.showMessageDialog(
                                getSupportFragmentManager(),
                                "Feedback",
                                "User successfully created");
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

    private void readUserInput() {
        mName = mBinding.name.getText().toString();
        mSurname = mBinding.surname.getText().toString();
        mEmail = mBinding.email.getText().toString();
        mPassword = mBinding.password.getText().toString();
        mRepeatPassword = mBinding.passwordRepeated.getText().toString();
    }

    private void resetInputs() {
        mBinding.email.setText("");
        mBinding.name.setText("");
        mBinding.surname.setText("");
        mBinding.password.setText("");
        mBinding.passwordRepeated.setText("");
    }

    private void setUserDisplayName(){
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(mName+" "+mSurname)
                .build();

        mAuth.getCurrentUser().updateProfile(profileUpdates);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}