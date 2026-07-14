package userService.dao.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import userService.TestDB;
import userService.dao.UserDao;
import userService.entity.User;
import userService.exception.UserValidationException;
import userService.exception.UserNotFoundException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TestUserDaoImpl extends TestDB {

    private UserDao userDao;

    @BeforeEach
    void setUp() {
        userDao = new UserDaoImpl();
    }

    @Test
    @DisplayName("При сохранении двух Пользователей с одинаковым email, должна быть ошибка UserValidationException")
    void testSave_ShouldThrowUserValidationException_WhenEmailDuplicated() {
        User user1 = new User("Ivan", "duplicate@example.com", 20);
        User user2 = new User("Nikola", "duplicate@example.com", 30);

        userDao.save(user1);

        assertThrows(UserValidationException.class, () -> userDao.save(user2));

        List<User> allUsers = userDao.findAll();
        assertEquals(1, allUsers.size());
    }

    @Test
    @DisplayName("При сохранении Пользователя с null полем в имени, должна быть ошибка UserValidationException")
    void testSave_ShouldThrowUserValidationException_WhenNameIsNull() {
        User userWithNullName = new User(null, "nullname@example.com", 25);

        assertThrows(UserValidationException.class, () -> userDao.save(userWithNullName));
    }

    @Test
    @DisplayName("При поиске не существующего Пользователя по ID, должна быть ошибка UserNotFoundException")
    void testFindById_ShouldThrowUserNotFoundException_WhenUserDoesNotExist() {
        assertThrows(UserNotFoundException.class, () -> userDao.findById(999L));
    }

    @Test
    @DisplayName("После сохранении Пользователя, должна быть возможность найти его по ID")
    void testSaveAndFindById() {
        User user = new User("Ivan", "ivan@example.com", 29);
        userDao.save(user);

        User userFromDB = userDao.findById(user.getId());
        assertNotNull(userFromDB);
        assertEquals("Ivan", userFromDB.getName());
    }

    @Test
    @DisplayName("После сохранения двух Пользователей, БД должна вернуть список из двух объектов")
    void testFindAll() {
        userDao.save(new User("Ivan", "ivan@example.com", 20));
        userDao.save(new User("Nikola", "nikola@example.com", 21));

        List<User> users = userDao.findAll();
        assertEquals(2, users.size());
    }

    @Test
    @DisplayName("Тест обновдения данных в БД")
    void testUpdate() {
        User user = new User("Nikola", "nikola@example.com", 40);
        userDao.save(user);

        user.setName("Ivan");
        userDao.update(user);

        User updatedUser = userDao.findById(user.getId());
        assertEquals("Ivan", updatedUser.getName());
    }

    @Test
    @DisplayName("Тест удаления Пользователя по ID")
    void testDeleteById() {
        User user = new User("Ivan", "ivan@example.com", 18);
        userDao.save(user);
        Long id = user.getId();

        userDao.deleteById(id);
        assertThrows(UserNotFoundException.class, () -> userDao.findById(id));
    }
}