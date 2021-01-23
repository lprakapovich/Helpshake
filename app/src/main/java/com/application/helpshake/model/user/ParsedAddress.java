package com.application.helpshake.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParsedAddress {
    private String country;
    private String state;
    private String city;
    private String address;
}
