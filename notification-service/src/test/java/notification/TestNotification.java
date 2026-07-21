package notification;

import dto.ActionType;
import dto.UserNotificationEvent;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class TestNotification extends TestKafkaAndMail {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    @DisplayName("Проверка API: Отправка сообщения при создании")
    void testDirectApiNotification_CreateAction_ShouldSendEmail() throws Exception {
        String email = "api-user-create@example.com";
        UserNotificationEvent event = new UserNotificationEvent(email, ActionType.CREATE);

        ResponseEntity<Void> response = restTemplate.postForEntity("/api/v1/notifications/send", event, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages).hasSize(1);

        MimeMessage firstMessage = receivedMessages[0];

        assertThat(java.util.Arrays.toString(firstMessage.getAllRecipients())).contains(email);
        assertThat(firstMessage.getSubject()).isEqualTo("Уведомление от Вашего Сайта");
        assertThat(firstMessage.getContent().toString()).contains("успешно создан");

    }

    @Test
    @DisplayName("Проверка Kafka: Отправка сообщения при удалении")
    void testKafkaNotificationConsumer_DeleteAction_ShouldSendEmail() {
        String email = "kafka-user-delete@example.com";
        UserNotificationEvent event = new UserNotificationEvent(email, ActionType.DELETE);

        kafkaTemplate.send("user-notifications-topic", event);

        await().atMost(5, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(200))
                .untilAsserted(() -> {
                    MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
                    assertThat(receivedMessages).hasSize(1);

                    MimeMessage firstMessage = receivedMessages[0];
                    assertThat(java.util.Arrays.toString(firstMessage.getAllRecipients())).contains(email);
                    assertThat(firstMessage.getSubject()).isEqualTo("Уведомление от Вашего Сайта");
                    assertThat(firstMessage.getContent().toString()).contains("Ваш аккаунт был удалён");
                });

    }
}