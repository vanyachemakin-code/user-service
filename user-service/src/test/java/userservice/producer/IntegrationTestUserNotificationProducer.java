package userservice.producer;

import dto.ActionType;
import dto.UserNotificationEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import userservice.event.UserInternalEvent;
import userservice.testDatabase.TestDB;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(OutputCaptureExtension.class)
public class IntegrationTestUserNotificationProducer extends TestDB {

    @Autowired
    private UserNotificationProducer userNotificationProducer;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Captor
    private ArgumentCaptor<UserNotificationEvent> eventCaptor;

    private final String email = "test@example.com";

    @Test
    @DisplayName("Успешная отправка и подтверждение приема сообщения контейнером Кафки")
    void handleUserInternalEvent_shouldSuccessfullySendToKafkaContainer_whenKafkaIsAvailable() {
        UserInternalEvent internalEvent = new UserInternalEvent(email, ActionType.CREATE);

        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq("user-notifications-topic"), any())).thenReturn(future);

        userNotificationProducer.handleUserInternalEvent(internalEvent);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(kafkaTemplate, times(1)).send(eq("user-notifications-topic"), eventCaptor.capture());

            UserNotificationEvent sentEvent = eventCaptor.getValue();
            assertThat(sentEvent.email()).isEqualTo(email);
            assertThat(sentEvent.actionType()).isEqualTo(ActionType.CREATE);
        });
    }

    @Test
    @DisplayName("CircuitBreaker ловит падение контейнера Кафки и успешно уходит в fallback")
    void handleUserInternalEvent_shouldTriggerCircuitBreakerFallback_whenKafkaContainerStops(CapturedOutput output) {
        UserInternalEvent internalEvent = new UserInternalEvent(email, ActionType.DELETE);

        CompletableFuture<SendResult<String, Object>> failingFuture = new CompletableFuture<>();
        failingFuture.completeExceptionally(new ExecutionException("Connection refused", new Throwable()));

        when(kafkaTemplate.send(eq("user-notifications-topic"), any())).thenReturn(failingFuture);

        userNotificationProducer.handleUserInternalEvent(internalEvent);

        await().atMost(7, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(output.getOut())
                    .contains("Kafka недоступна. Письмо для: " + email);
        });
    }
}