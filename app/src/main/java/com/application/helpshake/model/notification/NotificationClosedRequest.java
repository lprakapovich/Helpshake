package com.application.helpshake.model.notification;

import com.application.helpshake.model.user.BaseUser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationClosedRequest extends Notification {
    String closedRequestId;

    public NotificationClosedRequest(String uid,
                                     BaseUser from,
                                     BaseUser to,
                                     String title,
                                     String message,
                                     Boolean isChecked,
                                     String id) {
        super(uid, from, to, title, message, isChecked);
        closedRequestId = id;
    }
}
