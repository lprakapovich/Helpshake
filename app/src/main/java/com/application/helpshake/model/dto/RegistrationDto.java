package com.application.helpshake.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Used to validate user input during the registration
 */
@Data
@AllArgsConstructor
public class RegistrationDto {
    private String name;
    private String surname;
    private String email;
    private String password;
    private String confirmPassword;
}
