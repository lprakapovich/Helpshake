package com.application.helpshake.model.notification;

import com.application.helpshake.model.user.BaseUser;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class NotificationClosedRequest extends Notification {
    String closedRequestId;

    public NotificationClosedRequest(BaseUser from,
                                     BaseUser to,
                                     String title,
                                     String message,
                                     Boolean isChecked,
                                     String id) {
        super(from, to, title, message, isChecked);
        closedRequestId = id;
    }
}
