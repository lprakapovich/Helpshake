package com.application.helpshake.model;

import android.media.Image;

import java.util.List;

import lombok.Data;

@Data
public class HelpSeekerRequest {

    String requestId;

    String helpSeekerUid;

    String helpSeekerName;

    String helpSeekerSurname;

    List<HelpCategory> helpCategories;

    Status status;

    String comment;

    int photoId;

    //Location location;

    public HelpSeekerRequest() {}

    public HelpSeekerRequest(String requestId,
                             String helpSeekerUid,
                             String helpSeekerName,
                             String helpSeekerSurname,
                             List<HelpCategory> helpCategories,
                             Status status,
                             String comment) {

        this.requestId = requestId;
        this.helpSeekerUid = helpSeekerUid;
        this.helpSeekerName = helpSeekerName;
        this.helpSeekerSurname = helpSeekerSurname;
        this.helpCategories = helpCategories;
        this.status = status;
        this.comment = comment;
    }

}

