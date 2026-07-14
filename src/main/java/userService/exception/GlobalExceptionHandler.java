package userService.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public static void handler(Throwable e) {
        if (e instanceof UserValidationException) {
            logger.warn("Ошибка валидации данных: {}", e.getMessage());
        } else if (e instanceof  UserNotFoundException) {
            logger.warn("Данные не найдены: {}", e.getMessage());
        } else if (e instanceof UserAppException) {
            logger.error("Внутренняя ошибка сервиса: {}", e.getMessage());
        } else {
            logger.error("Критический сбой системы!", e);
        }
    }
}
