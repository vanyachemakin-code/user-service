package userservice.producer;

import dto.ActionType;
import dto.UserNotificationEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserNotificationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic}")
    private String topicName;

    @CircuitBreaker(name = "kafkaProducer", fallbackMethod = "fallbackNotification")
    public void sendNotificationEvent(String email, ActionType actionType) {
        log.info("Отправка Kafka Event...");
        UserNotificationEvent event = new UserNotificationEvent(email, actionType);
        kafkaTemplate.send(topicName, event);
        log.info("Kafka Event успешно отправлен. Отправлено письмо на почту: {}", email);
    }

    private void fallbackNotification(String email, ActionType actionType, Throwable t) {
        log.warn("Kafka недоступна. Письмо для: {} ({}) не отправлено! Причина: {}",
                email, actionType, t.getMessage());
    }
}
