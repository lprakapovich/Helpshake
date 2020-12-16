package com.application.helpshake.model;
import lombok.Data;

@Data
public class VolunteerEnrollment {
    String volunteerUid;

    String requestId;

    public VolunteerEnrollment(String volunteerUid, String requestId) {
        this.requestId = requestId;
        this.volunteerUid = volunteerUid;
    }
}
