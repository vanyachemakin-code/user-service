package notification.consumer;

import dto.UserNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notification.exception.NotificationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import notification.service.EmailService;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaNotificationListener {

    private final EmailService emailService;

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(UserNotificationEvent event) {
        log.info("Получено событие из Kafka. Email: {}, Действие: {}", event.email(), event.actionType());

        try {
            emailService.sendNotification(event);
        } catch (NotificationException e) {

            log.error("Не удалось отправить email по событию из Kafka", e);
        }
    }
}