package userService.exception;

import java.text.MessageFormat;

public class UserEmailValidationException extends UserAppException {

    public UserEmailValidationException(String email) {
        super(MessageFormat.format("Пользователь с email: {0}, уже зарегистрирован!", email));
    }
}
