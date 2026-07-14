package userService.exception;

import java.text.MessageFormat;

public class UserValidationException extends UserAppException {
    public UserValidationException(String message) {
        super(message);
    }

    public UserValidationException(String fieldName, String reason) {
        super(MessageFormat.format("Ошибка валидации Пользователя! Поле [{0}] {1}", fieldName, reason));
    }
}
