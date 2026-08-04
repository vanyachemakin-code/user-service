package notification.exception;

import java.text.MessageFormat;

public class NotificationException extends RuntimeException {

    public NotificationException(String email) {
        super(MessageFormat.format("Не удалось отправить сообщения на email: {0}!", email));
    }
}