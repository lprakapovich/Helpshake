package com.application.helpshake.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.R;
import com.application.helpshake.databinding.ActivityHelpSeekerProfilePageBinding;
import com.application.helpshake.databinding.ActivityVolunteerProfilePageBinding;
import com.application.helpshake.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class VolunteerProfilePage extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    private ActivityVolunteerProfilePageBinding volunteerProfileBinding ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        volunteerProfileBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_volunteer_profile_page);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        String name = mUser.getDisplayName();
        volunteerProfileBinding.setNameAndSurname(name);
    }
}