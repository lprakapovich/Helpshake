package com.application.helpshake.model.request;

import com.google.type.DateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompletedRequest extends PublishedHelpRequest{

    private DateTime completionDate;
    private int ratings;
    private String feedback;
}
