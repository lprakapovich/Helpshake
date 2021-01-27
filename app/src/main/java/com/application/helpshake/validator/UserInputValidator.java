package com.application.helpshake.validator;

import com.application.helpshake.model.enums.ValidationResult;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.opencensus.common.Function;

import static com.application.helpshake.model.enums.ValidationResult.EMPTY_INPUT;
import static com.application.helpshake.model.enums.ValidationResult.INVALID_PHONE;
import static com.application.helpshake.model.enums.ValidationResult.SUCCESS;

public interface UserInputValidator extends Function<String, ValidationResult> {

    static UserInputValidator isNumberValid() {
        return phoneNumber -> {

            String patterns
                    = "^(\\+\\d{1,3}( )?)?((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4}$"
                    + "|^(\\+\\d{1,3}( )?)?(\\d{3}[ ]?){2}\\d{3}$"
                    + "|^(\\+\\d{1,3}( )?)?(\\d{3}[ ]?)(\\d{2}[ ]?){2}\\d{2}$";

            Pattern pattern = Pattern.compile(patterns);
            Matcher matcher = pattern.matcher(phoneNumber);
            return matcher.matches() ? SUCCESS : INVALID_PHONE;
        };
    }

    static UserInputValidator isNotEmpty() {
        return input -> StringUtils.isNotBlank(input) ? SUCCESS : EMPTY_INPUT;
    }

    default UserInputValidator and (UserInputValidator other) {
        return input -> {
            ValidationResult result = this.apply(input);
            return result.equals(SUCCESS) ? other.apply(input) : result;
        };
    }
}
