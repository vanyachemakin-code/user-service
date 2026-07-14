package userService.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import userService.dao.UserDao;
import userService.entity.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService userService;

    @Test
    void testSave_ShouldInvokeDaoSave() {
        User user = new User("Ivan", "ivan@example.com", 25);

        userService.save(user);
        verify(userDao, times(1)).save(user);
    }

    @Test
    void testFindById_ShouldReturnUser() {
        Long userId = 1L;
        User expectedUser = new User("Nikola", "nikola@example.com", 30);
        expectedUser.setId(userId);

        when(userDao.findById(userId)).thenReturn(expectedUser);

        User actualUser = userService.findById(userId);

        assertNotNull(actualUser);
        assertEquals(expectedUser.getName(), actualUser.getName());
        verify(userDao, times(1)).findById(userId);
    }

    @Test
    void testFindAll_ShouldReturnList() {
        List<User> expectedList = List.of(
                new User("Ivan", "ivan@example.com", 22),
                new User("Nikola", "nikola@example.com", 35)
        );
        when(userDao.findAll()).thenReturn(expectedList);

        List<User> actualList = userService.findAll();

        assertEquals(2, actualList.size());
        verify(userDao, times(1)).findAll();
    }

    @Test
    void testUpdate_ShouldInvokeDaoUpdate() {
        User user = new User("Nikola", "nikola@example.com", 28);
        user.setId(1L);

        userService.update(user);
        verify(userDao, times(1)).update(user);
    }

    @Test
    void testDeleteById_ShouldInvokeDaoDelete() {
        Long userId = 1L;

        userService.deleteById(userId);
        verify(userDao, times(1)).deleteById(userId);
    }
}