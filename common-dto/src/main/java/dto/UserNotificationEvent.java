package dto;

public record UserNotificationEvent(String email, ActionType actionType) {
}