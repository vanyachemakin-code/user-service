package userService.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UserRequestDto(

        @NotBlank
        @Size(max = 75)
        String name,

        @NotBlank
        @Email
        String email,

        @Positive
        Integer age) {
}
