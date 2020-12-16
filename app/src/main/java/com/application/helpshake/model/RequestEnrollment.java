package com.application.helpshake.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class RequestEnrollment {
    String requestId;

    String volunteerId;

    public RequestEnrollment(String requestId) {
        this.requestId = requestId;
        volunteerId = "";
    }

    public String getVolunteerId() {
        return volunteerId;
    }
}
