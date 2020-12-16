package com.application.helpshake.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerRequest {
    User volunteer;
    HelpSeekerRequest request;
}
