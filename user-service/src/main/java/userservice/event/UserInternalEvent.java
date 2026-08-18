package userservice.event;

import dto.ActionType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserInternalEvent (@NotBlank
                                 @Email
                                 String email,

                                 @NotNull
                                 ActionType actionType) {
}
