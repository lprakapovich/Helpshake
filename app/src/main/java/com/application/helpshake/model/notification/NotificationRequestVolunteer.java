package com.application.helpshake.model.notification;

import com.application.helpshake.model.user.BaseUser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestVolunteer extends Notification {
    String notificationRequestId;

    public NotificationRequestVolunteer(String uid,
                                        BaseUser from,
                                        BaseUser to,
                                        String title,
                                        String message,
                                        Boolean isChecked,
                                        String id) {
        super(uid, from, to, title, message, isChecked);
        notificationRequestId = id;
    }

}
