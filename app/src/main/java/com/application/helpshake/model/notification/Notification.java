package com.application.helpshake.model.notification;

import com.application.helpshake.model.user.BaseUser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Notification {

    protected String uid;
    protected BaseUser from;
    protected BaseUser to;
    protected String title;
    protected String message;
    protected boolean isChecked;
}
