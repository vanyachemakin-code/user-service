package dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserNotificationEvent(

        @NotBlank
        @Email
        String email,

        @NotNull
        ActionType actionType) {
}