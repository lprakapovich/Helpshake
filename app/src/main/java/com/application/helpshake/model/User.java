package com.application.helpshake.model;

import lombok.Data;

@Data
public class User {

    public User() {};

    public User(String name, String surname, String email, Role role) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.role = role;
    }

    private String name;
    private String surname;
    private String email;
    private Role role;
}
