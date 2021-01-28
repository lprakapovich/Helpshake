package com.application.helpshake.model.request;

import com.application.helpshake.model.enums.Status;
import com.application.helpshake.model.user.Address;
import com.application.helpshake.model.user.BaseUser;
import com.google.firebase.firestore.GeoPoint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class represents a full request document
 * We can retrieve all the necessary data about help seeker, request itself and the volunteer
 */

/**
 * Document structure will be as follows (see in the FireStore):
 *      - request
 *                  - help request
 *                                  - title
 *                                  - description
 *                                  - categories
 *                  - help seeker
 *                                  - name
 *                                  - surname
 *                                  - uid
 *                  - uid
 *
 *      - volunteer
 *                  - name
 *                  - surname
 *                  - uid and etc.
 *
 *      - status
 *      - uid
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublishedHelpRequest {

    UserHelpRequest request;
    BaseUser volunteer;
    Status status;
    String uid;
    float ratings;
}
