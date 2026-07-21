package userService.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import userService.dto.UserRequestDto;
import userService.dto.UserResponseDto;
import userService.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user-service")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/user/add")
    public ResponseEntity<Void> create(@Valid @RequestBody UserRequestDto userRequestDto) {
        userService.save(userRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<UserResponseDto> getById(@PathVariable Long id) {
        UserResponseDto userResponseDto = userService.findById(id);
        return ResponseEntity.ok(userResponseDto);
    }

    @GetMapping("/user/list")
    public ResponseEntity<List<UserResponseDto>> getAll() {
        List<UserResponseDto> userResponseDtoList = userService.findAll();
        return ResponseEntity.ok(userResponseDtoList);
    }

    @PutMapping("/user/{id}/update")
    public ResponseEntity<UserResponseDto> update(@PathVariable Long id, @Valid @RequestBody UserRequestDto userRequestDto) {
        UserResponseDto updatedUser = userService.update(id, userRequestDto);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/user/{id}/delete")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
