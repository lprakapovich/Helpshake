package com.application.helpshake.model;

import com.application.helpshake.model.enums.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class represents a volunteers feedback (offer help) to one of the requests
 * We can retrieve all the necessary data about hel seeker, request itself and the volunteer
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublishedHelpRequest {

    UserHelpRequest request;
    BaseUser volunteer;
    Status status;
    String uid;
}
