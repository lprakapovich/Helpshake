package com.application.helpshake.model;

import java.util.List;

import lombok.Data;

@Data
public class HelpSeekerRequest {

    String helpSeekerUid;

    String helpSeekerName;

    String helpSeekerSurname;

    List<HelpCategory> helpCategories;

    Status status;

    String comment;

    //Location location;

    public HelpSeekerRequest() {}

    public HelpSeekerRequest(String helpSeekerUid,
                             String helpSeekerName,
                             String helpSeekerSurname,
                             List<HelpCategory> helpCategories,
                             Status status,
                             String comment) {

        this.helpSeekerUid = helpSeekerUid;
        this.helpSeekerName = helpSeekerName;
        this.helpSeekerSurname = helpSeekerSurname;
        this.helpCategories = helpCategories;
        this.status = status;
        this.comment = comment;
    }
}

