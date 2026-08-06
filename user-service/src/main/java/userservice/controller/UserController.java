package userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import userservice.dto.UserRequestDto;
import userservice.dto.UserResponseDto;
import userservice.service.UserService;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "User", description = "Операции с Пользователями.")
@RestController
@RequestMapping("/api/v1/user-service")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Создание Пользователя.")
    @PostMapping("/user/add")
    public ResponseEntity<EntityModel<UserResponseDto>> create(@Valid @RequestBody UserRequestDto userRequestDto) {
        UserResponseDto savedUser = userService.save(userRequestDto);
        EntityModel<UserResponseDto> resource = toResource(savedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    @Operation(summary = "Получение Пользователя по ID.")
    @GetMapping("/user/{id}")
    public ResponseEntity<EntityModel<UserResponseDto>> getById(@PathVariable Long id) {
        UserResponseDto userResponseDto = userService.findById(id);
        return ResponseEntity.ok(toResource(userResponseDto));
    }

    @Operation(summary = "Получение списка всех Пользователей.")
    @GetMapping("/user/list")
    public ResponseEntity<CollectionModel<EntityModel<UserResponseDto>>> getAll() {
        List<UserResponseDto> userResponseDtoList = userService.findAll();

        List<EntityModel<UserResponseDto>> resources = userResponseDtoList.stream()
                .map(this::toResource)
                .toList();

        CollectionModel<EntityModel<UserResponseDto>> collection = CollectionModel.of(resources,
                linkTo(methodOn(UserController.class).getAll()).withSelfRel());
        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Обновление данных Пользователя по ID.")
    @PutMapping("/user/{id}/update")
    public ResponseEntity<EntityModel<UserResponseDto>> update(@PathVariable Long id, @Valid @RequestBody UserRequestDto userRequestDto) {
        UserResponseDto updatedUser = userService.update(id, userRequestDto);
        return ResponseEntity.ok(toResource(updatedUser));
    }

    @Operation(summary = "Удаление Пользователя по ID.")
    @DeleteMapping("/user/{id}/delete")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<UserResponseDto> toResource(UserResponseDto userResponseDto) {
        EntityModel<UserResponseDto> resource = EntityModel.of(userResponseDto);

        resource.add(linkTo(methodOn(UserController.class).getById(userResponseDto.id())).withSelfRel());
        resource.add(linkTo(methodOn(UserController.class).getAll()).withRel("all-users"));
        resource.add(linkTo(methodOn(UserController.class).update(userResponseDto.id(), null)).withRel("update"));
        resource.add(linkTo(methodOn(UserController.class).deleteById(userResponseDto.id())).withRel("delete"));
        return resource;
    }
}
