package com.application.helpshake.model;

import com.application.helpshake.model.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseUser {

    private String uid;
    private String name;
    private String lastName;
    private Role role;
    private String email;
    private String phoneNumber;
    private Address address;

    public String getFullName() {
        return name + " " + lastName;
    }
}

