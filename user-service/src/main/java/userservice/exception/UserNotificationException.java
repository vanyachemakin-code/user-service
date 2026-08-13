package userservice.exception;

import dto.UserNotificationEvent;

import java.text.MessageFormat;

public class UserNotificationException extends RuntimeException {

    public UserNotificationException(UserNotificationEvent notificationEvent, String reason) {
        super(MessageFormat.format("Kafka недоступна. Письмо для: {0} ({1}) не отправлено! {2}",
                notificationEvent.email(), notificationEvent.actionType(), reason));
    }
}
