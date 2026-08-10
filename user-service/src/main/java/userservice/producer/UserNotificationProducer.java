package userservice.producer;

import dto.UserNotificationEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import userservice.event.UserInternalEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserNotificationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic}")
    private String topicName;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @CircuitBreaker(name = "kafkaProducer", fallbackMethod = "fallbackNotification")
    public void handleUserInternalEvent(UserInternalEvent userInternalEvent) {
        log.info("Отправка Kafka Event...");

        UserNotificationEvent event = new UserNotificationEvent(userInternalEvent.email(), userInternalEvent.actionType());
        kafkaTemplate.send(topicName, event);

        log.info("Kafka Event успешно отправлен. Отправлено письмо на почту: {}", userInternalEvent.email());
    }

    private void fallbackNotification(UserInternalEvent event, Throwable t) {
        log.warn("Kafka недоступна. Письмо для: {} ({}) не отправлено! Причина: {}",
                event.email(), event.actionType(), t.getMessage());
    }
}