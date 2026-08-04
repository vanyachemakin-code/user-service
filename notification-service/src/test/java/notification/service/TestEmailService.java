package notification.service;

import dto.ActionType;
import dto.UserNotificationEvent;
import notification.exception.NotificationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestEmailService {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    @DisplayName("Тест формирования письма при создании Пользователя")
    void sendNotification_WhenActionIsCreate_ShouldSendCorrectEmail() {
        String email = "test-create@example.com";
        UserNotificationEvent event = new UserNotificationEvent(email, ActionType.CREATE);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        emailService.sendNotification(event);

        verify(javaMailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getTo()).containsExactly(email);
        assertThat(sentMessage.getSubject()).isEqualTo("Уведомление от Вашего Сайта");
        assertThat(sentMessage.getText()).contains("успешно создан");
    }

    @Test
    @DisplayName("Тест формирования письма при удалении Пользователя")
    void sendNotification_WhenActionIsDelete_ShouldSendCorrectEmail() {
        String email = "test-delete@example.com";
        UserNotificationEvent event = new UserNotificationEvent(email, ActionType.DELETE);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        emailService.sendNotification(event);

        verify(javaMailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getTo()).containsExactly(email);
        assertThat(sentMessage.getText()).contains("Ваш аккаунт был удалён");
    }

    @Test
    @DisplayName("Выброс NotificationException при сбое сервиса")
    void sendNotification_WhenMailSenderFails_ShouldThrowNotificationExceptionWithCorrectMessage() {
        String targetEmail = "fail@example.com";
        UserNotificationEvent event = new UserNotificationEvent(targetEmail, ActionType.CREATE);

        doThrow(new RuntimeException("SMTP Connection refused")).when(javaMailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> emailService.sendNotification(event))
                .isInstanceOf(NotificationException.class)
                .hasMessage("Не удалось отправить сообщения на email: fail@example.com!");
    }

}
