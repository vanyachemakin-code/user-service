package notification.controller;

import dto.UserNotificationEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import notification.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<String> send(@Valid @RequestBody UserNotificationEvent event) {
        emailService.sendNotification(event);

        return ResponseEntity.ok().build();
    }
}