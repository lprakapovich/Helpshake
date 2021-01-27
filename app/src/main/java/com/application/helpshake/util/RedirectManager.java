package com.application.helpshake.util;

import android.app.Activity;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.application.helpshake.model.enums.Role;
import com.application.helpshake.model.user.BaseUser;
import com.application.helpshake.view.helpseeker.HelpSeekerHomeActivity;
import com.application.helpshake.view.volunteer.CurrentHelpOffersActivity;
import com.application.helpshake.view.volunteer.VolunteerHomeActivity;

public class RedirectManager  {
    public static Class<? extends AppCompatActivity> redirectToHome(BaseUser user) {
        return user.getRole().equals(Role.HelpSeeker) ? HelpSeekerHomeActivity.class : VolunteerHomeActivity.class;
    }

    public static void redirectTo(AppCompatActivity from, Class<? extends AppCompatActivity> to) {
        from.startActivity(new Intent(from, to));
    }
}
