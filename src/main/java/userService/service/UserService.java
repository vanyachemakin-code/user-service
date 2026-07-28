package userService.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import userService.repository.UserRepository;
import userService.dto.UserRequestDto;
import userService.dto.UserResponseDto;
import userService.entity.UserEntity;
import userService.exception.UserNotFoundException;
import userService.exception.UserEmailValidationException;
import userService.mapper.UserMapper;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;

    public void save(UserRequestDto userRequestDto) {
        log.info("Сохранение Пользователя: {}...", userRequestDto.name());

        if (userRepository.existsByEmail(userRequestDto.email())) {
            throw new UserEmailValidationException(userRequestDto.email());
        }
        UserEntity userEntity = mapper.toEntity(userRequestDto);
        userRepository.save(userEntity);

        log.info("Пользователь: {}, успешно сохранен в БД", userRequestDto.name());
    }

    public UserResponseDto findById(Long id) {
        log.info("Поиск Пользователя с ID: {}...", id);

        return userRepository.findById(id).map(mapper::toDto).orElseThrow(() -> new UserNotFoundException(id));
    }

    public List<UserResponseDto> findAll() {
        log.info("Поиск всех Пользователей в БД...");

        return userRepository.findAll().stream().map(mapper::toDto).toList();
    }

    @Transactional
    public UserResponseDto update(Long id, UserRequestDto userRequestDto) {
        log.info("Обновление данных Пользователя с ID: {}...", id);

        UserEntity userEntity = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        userEntity.setName(userRequestDto.name());
        userEntity.setEmail(userRequestDto.email());
        userEntity.setAge(userRequestDto.age());

        log.info("Пользователь с ID: {}, успешно обновлен.", id);
        return mapper.toDto(userEntity);
    }

    @Transactional
    public void deleteById(Long id) {
        log.info("Удаление Пользователя с ID: {}...", id);

        UserEntity userEntity = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        userRepository.delete(userEntity);

        log.info("Пользователь с ID: {}, успешно удален.", id);
    }
}
