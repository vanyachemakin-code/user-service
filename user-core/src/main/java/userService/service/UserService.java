package userService.service;

import dto.ActionType;
import dto.UserNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import userService.dao.UserDao;
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

    private final UserDao userDao;
    private final UserMapper mapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic}")
    private String topicName;

    public void save(UserRequestDto userRequestDto) {
        log.info("Сохранение Пользователя: {}...", userRequestDto.name());

        if (userDao.existsByEmail(userRequestDto.email())) {
            throw new UserEmailValidationException(userRequestDto.email());
        }
        UserEntity userEntity = mapper.toEntity(userRequestDto);
        userDao.save(userEntity);

        log.info("Отправка Kafka Event...");
        UserNotificationEvent event = new UserNotificationEvent(userRequestDto.email(), ActionType.CREATE);
        kafkaTemplate.send(topicName, event);
        log.info("Kafka Event успешно отправлен. Отправлено письмо на почту: {}, о регистрации.", userRequestDto.email());

        log.info("Пользователь: {}, успешно сохранен в БД", userRequestDto.name());
    }

    public UserResponseDto findById(Long id) {
        log.info("Поиск Пользователя с ID: {}...", id);

        return userDao.findById(id).map(mapper::toDto).orElseThrow(() -> new UserNotFoundException(id));
    }

    public List<UserResponseDto> findAll() {
        log.info("Поиск всех Пользователей в БД...");

        return userDao.findAll().stream().map(mapper::toDto).toList();
    }

    @Transactional
    public UserResponseDto update(Long id, UserRequestDto userRequestDto) {
        log.info("Обновление данных Пользователя с ID: {}...", id);

        UserEntity userEntity = userDao.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        userEntity.setName(userRequestDto.name());
        userEntity.setEmail(userRequestDto.email());
        userEntity.setAge(userRequestDto.age());

        log.info("Пользователь с ID: {}, успешно обновлен.", id);
        return mapper.toDto(userEntity);
    }

    @Transactional
    public void deleteById(Long id) {
        log.info("Удаление Пользователя с ID: {}...", id);

        UserEntity userEntity = userDao.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        userDao.delete(userEntity);

        log.info("Отправка Kafka Event...");
        UserNotificationEvent event = new UserNotificationEvent(userEntity.getEmail(), ActionType.DELETE);
        kafkaTemplate.send(topicName, event);
        log.info("Kafka Event успешно отправлен. Отправлено письмо на почту: {}, об удалении.", userEntity.getEmail());

        log.info("Пользователь с ID: {}, успешно удален.", id);
    }
}
