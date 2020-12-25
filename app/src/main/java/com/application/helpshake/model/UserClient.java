package com.application.helpshake.model;

import android.app.Application;

/**
 * Singleton representing a currently authenticated user
 * Instead of fetching the user from thr database, we set application context user (after each login),
 * and access him through the app context, e.g.
 *
 * BaseUser user = ((UserClient)(getApplicationContext)).getCurrentUser();
 */

public class UserClient extends Application {

    private BaseUser currentUser = null;

    public void setCurrentUser(BaseUser user) {
        currentUser = user;
    }

    public BaseUser getCurrentUser() {
        return currentUser;
    }
}
