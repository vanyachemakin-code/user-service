package userservice.service;

import dto.ActionType;
import dto.UserNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import userservice.repository.UserRepository;
import userservice.dto.UserRequestDto;
import userservice.dto.UserResponseDto;
import userservice.entity.UserEntity;
import userservice.exception.UserNotFoundException;
import userservice.exception.UserEmailValidationException;
import userservice.mapper.UserMapper;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic}")
    private String topicName;

    @Transactional
    public UserResponseDto save(UserRequestDto userRequestDto) {
        log.info("Сохранение Пользователя: {}...", userRequestDto.name());

        if (userRepository.existsByEmail(userRequestDto.email())) {
            throw new UserEmailValidationException(userRequestDto.email());
        }
        UserEntity userEntity = mapper.toEntity(userRequestDto);
        UserEntity savedUser = userRepository.save(userEntity);

        log.info("Отправка Kafka Event...");
        UserNotificationEvent event = new UserNotificationEvent(userRequestDto.email(), ActionType.CREATE);
        kafkaTemplate.send(topicName, event);
        log.info("Kafka Event успешно отправлен. Отправлено письмо на почту: {}, о регистрации.", userRequestDto.email());

        log.info("Пользователь: {}, успешно сохранен в БД", userRequestDto.name());
        return mapper.toDto(savedUser);
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

        if (userRepository.existsByEmailAndIdNot(userRequestDto.email(), id)) {
            throw new UserEmailValidationException(userRequestDto.email());
        }

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

        log.info("Отправка Kafka Event...");
        UserNotificationEvent event = new UserNotificationEvent(userEntity.getEmail(), ActionType.DELETE);
        kafkaTemplate.send(topicName, event);
        log.info("Kafka Event успешно отправлен. Отправлено письмо на почту: {}, об удалении.", userEntity.getEmail());

        log.info("Пользователь с ID: {}, успешно удален.", id);
    }
}
