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
    String requestTitle;

    public NotificationRequestVolunteer(String uid,
                                        BaseUser from,
                                        BaseUser to,
                                        String title,
                                        String message,
                                        Boolean isChecked,
                                        String id,
                                        String requestTitle) {
        super(uid, from, to, title, message, isChecked);
        this.notificationRequestId = id;
        this.requestTitle = requestTitle;
    }
}
