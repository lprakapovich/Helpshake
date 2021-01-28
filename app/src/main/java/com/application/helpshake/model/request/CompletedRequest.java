package com.application.helpshake.model.request;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Date;

import lombok.Data;

@Data
public class CompletedRequest {

    private PublishedHelpRequest publishedHelpRequest;
    private Date completionDate;
    private float ratings;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public CompletedRequest(PublishedHelpRequest request, float ratings) {
        this.ratings = ratings;
        this.completionDate = new Date();
        this.publishedHelpRequest = request;
    }
}
