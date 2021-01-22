package com.application.helpshake.validator;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.application.helpshake.model.dto.RegistrationDto;
import com.application.helpshake.validator.UserRegistrationValidator.ValidationResult;

import org.apache.commons.lang3.StringUtils;

import io.opencensus.common.Function;

import static com.application.helpshake.validator.UserRegistrationValidator.ValidationResult.EMPTY_INPUT;
import static com.application.helpshake.validator.UserRegistrationValidator.ValidationResult.INVALID_EMAIL;
import static com.application.helpshake.validator.UserRegistrationValidator.ValidationResult.PASSWORDS_NOT_MATCH;
import static com.application.helpshake.validator.UserRegistrationValidator.ValidationResult.SUCCESS;

@RequiresApi(api = Build.VERSION_CODES.N)
public interface UserRegistrationValidator extends Function<RegistrationDto, ValidationResult> {

    static UserRegistrationValidator isEmailValid () {
        return registrationData -> registrationData.getEmail().contains("@")
                ? SUCCESS : INVALID_EMAIL;
    }

    static UserRegistrationValidator isPasswordValid () {
        return registrationData -> registrationData.getPassword().equals(registrationData.getConformPassword())
                ? SUCCESS : PASSWORDS_NOT_MATCH;
    }

    static UserRegistrationValidator isInputEmpty() {
        return registrationData ->
                StringUtils.isAnyEmpty(
                        registrationData.getEmail(),
                        registrationData.getConformPassword(),
                        registrationData.getName(),
                        registrationData.getSurname(),
                        registrationData.getPassword()
                ) ? EMPTY_INPUT : SUCCESS;
    }

    default UserRegistrationValidator and (UserRegistrationValidator other) {
        return registrationData -> {
            ValidationResult result = this.apply(registrationData);
            return result.equals(SUCCESS) ? other.apply(registrationData) : result;
        };
    }

    enum ValidationResult {
        SUCCESS,
        EMPTY_INPUT,
        INVALID_NAME,
        INVALID_EMAIL,
        PASSWORDS_NOT_MATCH
    }
}
