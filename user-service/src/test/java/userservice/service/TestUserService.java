package userservice.service;

import dto.ActionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import userservice.event.UserInternalEvent;
import userservice.repository.UserRepository;
import userservice.dto.UserRequestDto;
import userservice.dto.UserResponseDto;
import userservice.entity.UserEntity;
import userservice.exception.UserEmailValidationException;
import userservice.exception.UserNotFoundException;
import userservice.mapper.UserMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestUserService {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<UserInternalEvent> internalEventArgumentCaptor;

    @InjectMocks
    private UserService userService;

    private UserEntity entity;
    private UserResponseDto responseDto;
    private UserRequestDto requestDto;

    @BeforeEach
    void setUs() {
        entity = new UserEntity();
        entity.setId(1L);
        entity.setName("Ivan");
        entity.setEmail("ivan@example.com");
        entity.setAge(25);
        entity.setCreatedAt(LocalDateTime.now());

        responseDto = new UserResponseDto(1L, "Ivan", "ivan@example.com", 25, LocalDateTime.now());
        requestDto = new UserRequestDto("Ivan", "ivan@example.com", 25);
    }

    @Test
    @DisplayName("Сохранение Пользователя с уникальным email")
    void save_shouldSaveUser_whenEmailIsUnique() {
        when(userRepository.existsByEmail(requestDto.email())).thenReturn(false);
        when(userMapper.toEntity(requestDto)).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(entity);
        when(userMapper.toDto(entity)).thenReturn(responseDto);

        UserResponseDto result = userService.save(requestDto);

        verify(userRepository, times(1)).existsByEmail(requestDto.email());
        verify(userMapper, times(1)).toEntity(requestDto);
        verify(userRepository, times(1)).save(entity);
        verify(eventPublisher, times(1)).publishEvent(internalEventArgumentCaptor.capture());

        UserInternalEvent capturedEvent = internalEventArgumentCaptor.getValue();
        assertThat(capturedEvent.email()).isEqualTo("ivan@example.com");
        assertThat(capturedEvent.actionType()).isEqualTo(ActionType.CREATE);
        assertThat(result).isEqualTo(responseDto);
    }

    @Test
    @DisplayName("Выброс ошибки при сохранении Пользователя с не уникальным email")
    void save_shouldThrowUserEmailValidationException_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail(requestDto.email())).thenReturn(true);

        assertThrows(UserEmailValidationException.class, () -> userService.save(requestDto));

        verify(userRepository, never()).save(any(UserEntity.class));
        verify(userMapper, never()).toEntity(any());
    }

    @Test
    @DisplayName("Поиск Пользователя по ID")
    void findById_shouldReturnUserResponseDto_whenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(userMapper.toDto(entity)).thenReturn(responseDto);

        UserResponseDto result = userService.findById(1L);

        assertThat(result).isEqualTo(responseDto);
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Выброс ошибки если Пользователь не найден")
    void findById_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findById(1L));
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Поиск всех Пользователей")
    void findAll_shouldReturnListOfUsers_whenUsersExist() {
        when(userRepository.findAll()).thenReturn(List.of(entity));
        when(userMapper.toDto(entity)).thenReturn(responseDto);

        List<UserResponseDto> result = userService.findAll();

        assertThat(result).hasSize(1).containsExactly(responseDto);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Обновление данных Пользователя")
    void update_shouldUpdateAndReturnUser_whenUserExists() {
        UserRequestDto updateRequest = new UserRequestDto("Petr", "petr@example.com", 30);
        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(userMapper.toDto(entity)).thenReturn(responseDto);

        UserResponseDto result = userService.update(1L, updateRequest);

        assertThat(entity.getName()).isEqualTo("Petr");
        assertThat(entity.getEmail()).isEqualTo("petr@example.com");
        assertThat(entity.getAge()).isEqualTo(30);
        assertThat(result).isEqualTo(responseDto);
    }

    @Test
    @DisplayName("Если Пользователь не найден при попытке обновить данные, должна выброситься ошибка")
    void update_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.update(1L, requestDto));
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Удаление Пользователя")
    void deleteById_shouldDeleteUser_whenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));

        userService.deleteById(1L);

        verify(userRepository, times(1)).delete(entity);
        verify(eventPublisher, times(1)).publishEvent(internalEventArgumentCaptor.capture());

        UserInternalEvent capturedEvent = internalEventArgumentCaptor.getValue();
        assertThat(capturedEvent.email()).isEqualTo("ivan@example.com");
        assertThat(capturedEvent.actionType()).isEqualTo(ActionType.DELETE);
    }

    @Test
    @DisplayName("Ошибка при удалении, если Пользователь не найден")
    void deleteById_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteById(1L));
        verify(userRepository, never()).delete(any(UserEntity.class));
    }
}