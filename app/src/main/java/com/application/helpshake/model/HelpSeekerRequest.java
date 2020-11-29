package com.application.helpshake.model;

import java.util.List;

import lombok.Data;

@Data
public class HelpSeekerRequest {

    String helpSeekerUid;

    List<HelpCategory> helpCategories;

    Status status;

    String comment;

    //Location location;

    public HelpSeekerRequest() {}

    public HelpSeekerRequest(String helpSeekerUid,
                             List<HelpCategory> helpCategories,
                             Status status,
                             String comment) {

        this.helpSeekerUid = helpSeekerUid;
        this.helpCategories = helpCategories;
        this.status = status;
        this.comment = comment;
    }
}

