package userService.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import userService.entity.UserEntity;
import userService.testDatabase.TestDB;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class TestUserController extends TestDB {

    @Test
    @DisplayName("Успешное создание пользователя")
    void shouldCreateUserSuccessfully() throws Exception {
        mockMvc.perform(post("/api/v1/user-service/user/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Алексей",
                                  "email": "alex@example.com",
                                  "age": 25
                                }
                                """))
                .andExpect(status().isCreated());

        List<UserEntity> users = userDao.findAll();
        assertThat(users).hasSize(1);

        UserEntity savedUser = users.get(0);
        assertThat(savedUser.getName()).isEqualTo("Алексей");
        assertThat(savedUser.getEmail()).isEqualTo("alex@example.com");
        assertThat(savedUser.getAge()).isEqualTo(25);
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Ошибка 400 при нарушении всех правил валидации UserRequestDto")
    void shouldReturnBadRequestWhenFieldsAreInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/user-service/user/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "email": "not-an-email-format",
                                  "age": -5
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Ошибка валидации данных"))
                .andExpect(jsonPath("$.details.name").exists())
                .andExpect(jsonPath("$.details.email").exists())
                .andExpect(jsonPath("$.details.age").exists());
    }

    @Test
    @DisplayName("Ошибка 409 при попытке сохранить дубликат email")
    void shouldReturnConflictWhenEmailExists() throws Exception {
        UserEntity existingUser = new UserEntity("Иван", "duplicate@example.com", 30);
        userDao.save(existingUser);

        mockMvc.perform(post("/api/v1/user-service/user/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Петр",
                                  "email": "duplicate@example.com",
                                  "age": 20
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Пользователь с email: duplicate@example.com, уже зарегистрирован!"));

    }

    @Test
    @DisplayName("Успешное получение по ID и маппинг в UserResponseDto")
    void shouldGetUserByIdSuccessfully() throws Exception {
        UserEntity user = new UserEntity("Мария", "maria@example.com", 22);
        UserEntity saved = userDao.save(user);

        mockMvc.perform(get("/api/v1/user-service/user/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Мария"))
                .andExpect(jsonPath("$.email").value("maria@example.com"))
                .andExpect(jsonPath("$.age").value(22))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("Ошибка 404 если пользователя нет в базе")
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/user-service/user/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Успешное получение списка всех пользователей")
    void shouldReturnListOfUsers() throws Exception {
        UserEntity user1 = new UserEntity("User1", "u1@ex.com", 18);
        UserEntity user2 = new UserEntity("User2", "u2@ex.com", 19);
        userDao.saveAll(List.of(user1, user2));

        mockMvc.perform(get("/api/v1/user-service/user/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("User1"))
                .andExpect(jsonPath("$[1].name").value("User2"));
    }

    @Test
    @DisplayName("Успешное обновление полей в рамках одной транзакции")
    void shouldUpdateUserSuccessfully() throws Exception {
        UserEntity oldUser = new UserEntity("Старое Имя", "old@example.com", 40);
        UserEntity saved = userDao.save(oldUser);

        mockMvc.perform(put("/api/v1/user-service/user/" + saved.getId() + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Новое Имя",
                                  "email": "new@example.com",
                                  "age": 41
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новое Имя"))
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.age").value(41));

        UserEntity updatedInDb = userDao.findById(saved.getId()).orElseThrow();
        assertThat(updatedInDb.getName()).isEqualTo("Новое Имя");
        assertThat(updatedInDb.getAge()).isEqualTo(41);
    }

    @Test
    @DisplayName("Успешное удаление существующего пользователя")
    void shouldDeleteUserSuccessfully() throws Exception {
        UserEntity userToDelete = new UserEntity("Удаляюсь", "delete@example.com", 50);
        UserEntity saved = userDao.save(userToDelete);

        mockMvc.perform(delete("/api/v1/user-service/user/" + saved.getId() + "/delete"))
                .andExpect(status().isNoContent());

        assertThat(userDao.findById(saved.getId())).isEmpty();
    }
}