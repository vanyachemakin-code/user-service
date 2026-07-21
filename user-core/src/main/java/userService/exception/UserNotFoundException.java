package userService.exception;

import java.text.MessageFormat;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super(MessageFormat.format("Пользователь с ID: {0}, не найден!", id));
    }
}
