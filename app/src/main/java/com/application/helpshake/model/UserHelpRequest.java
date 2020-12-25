package com.application.helpshake.model;

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
