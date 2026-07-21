package userService.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import userService.dao.UserDao;
import userService.dto.UserRequestDto;
import userService.dto.UserResponseDto;
import userService.entity.UserEntity;
import userService.exception.UserEmailValidationException;
import userService.exception.UserNotFoundException;
import userService.mapper.UserMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestUserService {

    @Mock
    private UserDao userDao;

    @Mock
    private UserMapper userMapper;

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
        when(userDao.existsByEmail(requestDto.email())).thenReturn(false);
        when(userMapper.toEntity(requestDto)).thenReturn(entity);

        userService.save(requestDto);

        verify(userDao, times(1)).existsByEmail(requestDto.email());
        verify(userMapper, times(1)).toEntity(requestDto);
        verify(userDao, times(1)).save(entity);
    }

    @Test
    @DisplayName("Выброс ошибки при сохранении Пользователя с не уникальным email")
    void save_shouldThrowUserEmailValidationException_whenEmailAlreadyExists() {
        when(userDao.existsByEmail(requestDto.email())).thenReturn(true);

        assertThrows(UserEmailValidationException.class, () -> userService.save(requestDto));

        verify(userDao, never()).save(any(UserEntity.class));
        verify(userMapper, never()).toEntity(any());
    }

    @Test
    @DisplayName("Поиск Пользователя по ID")
    void findById_shouldReturnUserResponseDto_whenUserExists() {
        when(userDao.findById(1L)).thenReturn(Optional.of(entity));
        when(userMapper.toDto(entity)).thenReturn(responseDto);

        UserResponseDto result = userService.findById(1L);

        assertThat(result).isEqualTo(responseDto);
        verify(userDao, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Выброс ошибки если Пользователь не найден")
    void findById_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        when(userDao.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findById(1L));
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Поиск всех Пользователей")
    void findAll_shouldReturnListOfUsers_whenUsersExist() {
        when(userDao.findAll()).thenReturn(List.of(entity));
        when(userMapper.toDto(entity)).thenReturn(responseDto);

        List<UserResponseDto> result = userService.findAll();

        assertThat(result).hasSize(1).containsExactly(responseDto);
        verify(userDao, times(1)).findAll();
    }

    @Test
    @DisplayName("Обновление данных Пользователя")
    void update_shouldUpdateAndReturnUser_whenUserExists() {
        UserRequestDto updateRequest = new UserRequestDto("Petr", "petr@example.com", 30);
        when(userDao.findById(1L)).thenReturn(Optional.of(entity));
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
        when(userDao.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.update(1L, requestDto));
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Удаление Пользователя")
    void deleteById_shouldDeleteUser_whenUserExists() {
        when(userDao.findById(1L)).thenReturn(Optional.of(entity));

        userService.deleteById(1L);
        verify(userDao, times(1)).delete(entity);
    }

    @Test
    @DisplayName("Ошибка при удалении, если Пользователь не найден")
    void deleteById_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        when(userDao.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteById(1L));
        verify(userDao, never()).delete(any(UserEntity.class));
    }
}