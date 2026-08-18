package userservice.producer;

import dto.ActionType;
import dto.UserNotificationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import userservice.event.UserInternalEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestUserNotificationProducer {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Captor
    private ArgumentCaptor<UserNotificationEvent> notificationEventArgumentCaptor;

    @InjectMocks
    private UserNotificationProducer userNotificationProducer;

    private final String testTopic = "user-notifications-topic";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userNotificationProducer, "topicName", testTopic);
    }

    @Test
    @DisplayName("Успешная отправка события в Kafka при обработке внутреннего события")
    void handleUserInternalEvent_shouldSendKafkaEvent_whenKafkaAvailable() {
        UserInternalEvent internalEvent = new UserInternalEvent("test@example.com", ActionType.CREATE);

        userNotificationProducer.handleUserInternalEvent(internalEvent);

        verify(kafkaTemplate, times(1)).send(eq(testTopic), notificationEventArgumentCaptor.capture());

        UserNotificationEvent sentEvent = notificationEventArgumentCaptor.getValue();
        assertThat(sentEvent.email()).isEqualTo("test@example.com");
        assertThat(sentEvent.actionType()).isEqualTo(ActionType.CREATE);
    }

    @Test
    @DisplayName("Метод не бросает исключение наружу, если отправка в Kafka завершилась ошибкой")
    void handleUserInternalEvent_shouldNotThrow_whenKafkaSendFails() {
        UserInternalEvent internalEvent = new UserInternalEvent("test@example.com", ActionType.DELETE);
        when(kafkaTemplate.send(eq(testTopic), any())).thenThrow(new RuntimeException("Kafka недоступна"));

        assertThrows(RuntimeException.class,
                () -> userNotificationProducer.handleUserInternalEvent(internalEvent));
    }
}