package com.application.helpshake.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HelpSeekerRequest {

    String requestId;
    Status status;
    String title;
    String comment;
    String helpSeekerUid;
    String helpSeekerName;
    String helpSeekerSurname;
    List<HelpCategory> helpCategories;
    int photoId;

    public HelpSeekerRequest(String requestId,
                             String helpSeekerUid,
                             String helpSeekerName,
                             String helpSeekerSurname,
                             List<HelpCategory> helpCategories,
                             Status status,
                             String title,
                             String comment) {

        this.requestId = requestId;
        this.helpSeekerUid = helpSeekerUid;
        this.helpSeekerName = helpSeekerName;
        this.helpSeekerSurname = helpSeekerSurname;
        this.helpCategories = helpCategories;
        this.status = status;
        this.title = title;
        this.comment = comment;
    }

}

