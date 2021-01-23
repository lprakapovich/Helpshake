package com.application.helpshake.model.user;

import android.app.Application;

import com.application.helpshake.model.user.BaseUser;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Singleton representing a currently authenticated user
 * Instead of fetching the user from thr database, we set application context user (after each login),
 * and access him through the app context, e.g.
 *
 * BaseUser user = ((UserClient)(getApplicationContext)).getCurrentUser();
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class UserClient extends Application {
    private BaseUser currentUser = null;
}
