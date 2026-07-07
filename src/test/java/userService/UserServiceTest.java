package userService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import userService.entity.User;
import userService.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest extends TestDB {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
    }

    @Test
    void testSave_ShouldGenerateIdAndSetCreatedAt() {
        User user = new User("Иван", "ivan@example.com", 25);

        userService.save(user);

        assertNotNull(user.getId());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void testSave_ShouldPersistDataCorrectly() {
        User user = new User("Мария", "maria@example.com", 30);

        userService.save(user);
        User savedUser = userService.findById(user.getId());

        assertNotNull(savedUser);
        assertEquals("Мария", savedUser.getName());
        assertEquals("maria@example.com", savedUser.getEmail());
        assertEquals(30, savedUser.getAge());
    }

    @Test
    void testFindById_ShouldReturnUser_WhenUserExists() {
        User user = new User("Алексей", "alex@example.com", 20);
        userService.save(user);

        User foundUser = userService.findById(user.getId());

        assertNotNull(foundUser);
        assertEquals(user.getId(), foundUser.getId());
    }

    @Test
    void testFindById_ShouldReturnNull_WhenUserDoesNotExist() {
        User foundUser = userService.findById(999L);

        assertNull(foundUser);
    }

    @Test
    void testFindAll_ShouldReturnEmptyList_WhenDbIsEmpty() {
        List<User> users = userService.findAll();

        assertTrue(users.isEmpty());
    }

    @Test
    void testFindAll_ShouldReturnAllUsers_WhenUsersExist() {
        userService.save(new User("Анна", "anna@example.com", 22));
        userService.save(new User("Петр", "petr@example.com", 35));

        List<User> users = userService.findAll();

        assertEquals(2, users.size());
    }

    @Test
    void testUpdate_ShouldModifyFields_WhenUserExists() {
        User user = new User("Елена", "elena@example.com", 27);
        userService.save(user);

        user.setName("Елена Обновленная");
        user.setAge(28);
        userService.update(user);

        User updatedUser = userService.findById(user.getId());
        assertEquals("Елена Обновленная", updatedUser.getName());
        assertEquals(28, updatedUser.getAge());
        assertEquals("elena@example.com", updatedUser.getEmail()); // Email не менялся
    }

    @Test
    void testDeleteById_ShouldRemoveUser_WhenUserExists() {
        User user = new User("Дмитрий", "dima@example.com", 40);
        userService.save(user);
        Long id = user.getId();

        userService.deleteById(id);
        User deletedUser = userService.findById(id);

        assertNull(deletedUser);
    }

    @Test
    void testDeleteById_ShouldNotThrowException_WhenUserDoesNotExist() {
        assertDoesNotThrow(() -> userService.deleteById(999L));
    }
}
