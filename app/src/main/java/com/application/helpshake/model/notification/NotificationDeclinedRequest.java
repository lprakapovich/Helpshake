package com.application.helpshake.model.notification;

import com.application.helpshake.model.user.BaseUser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDeclinedRequest extends Notification {
    String declinedRequestId;

    public NotificationDeclinedRequest(String uid,
                                       BaseUser from,
                                       BaseUser to,
                                       String title,
                                       String message,
                                       Boolean isChecked,
                                       String id) {
        super(uid, from, to, title, message, isChecked);
        declinedRequestId = id;
    }

}
