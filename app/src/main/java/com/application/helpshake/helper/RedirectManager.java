package com.application.helpshake.helper;

import androidx.appcompat.app.AppCompatActivity;

import com.application.helpshake.model.enums.Role;
import com.application.helpshake.model.user.BaseUser;
import com.application.helpshake.view.helpseeker.HelpSeekerHomeActivity;
import com.application.helpshake.view.volunteer.VolunteerHomeActivity;

public class RedirectManager  {
    public static Class<? extends AppCompatActivity> redirectTo(BaseUser user) {
        return user.getRole().equals(Role.HelpSeeker) ? HelpSeekerHomeActivity.class : VolunteerHomeActivity.class;
    }
}
