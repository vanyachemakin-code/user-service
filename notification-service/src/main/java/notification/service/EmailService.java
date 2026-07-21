package notification.service;

import dto.UserNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notification.exception.NotificationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;

    public void sendNotification(UserNotificationEvent event) {
        String subject = "Уведомление от Вашего Сайта";

        String text = switch (event.actionType()) {
            case CREATE -> "Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.";
            case DELETE -> "Здравствуйте! Ваш аккаунт был удалён.";
        };

        try {
            sendEmail(event.email(), subject, text);
        } catch (Exception e) {
            throw new NotificationException(event.email());
        }
    }

    private void sendEmail(String email, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(text);

        javaMailSender.send(message);
        log.info("Письмо успешно отправлено на адрес: {}", email);
    }
}