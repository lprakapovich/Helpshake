package com.application.helpshake.model.request;

import com.application.helpshake.model.request.HelpRequest;
import com.application.helpshake.model.user.BaseUser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserHelpRequest {

    BaseUser helpSeeker;
    HelpRequest helpRequest;
    String uid;
}
