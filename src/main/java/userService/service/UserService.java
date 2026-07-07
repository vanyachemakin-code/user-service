package userService.service;

import userService.dao.UserDao;
import userService.dao.impl.UserDaoImpl;
import userService.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDao userDao = new UserDaoImpl();

    public void save(User user) {
        logger.info("Сохранение Пользователя: {}...", user.getName());

        userDao.save(user);
    }

    public User findById(Long id) {
        logger.info("Поиск Пользователя с ID: {}...", id);

        return userDao.findById(id);
    }

    public List<User> findAll() {
        logger.info("Поиск всех Пользователей в БД...");

        return userDao.findAll();
    }

    public void update(User user) {
        logger.info("Обновление данных Пользователя с ID: {}...", user.getId());

        userDao.update(user);
    }

    public void deleteById(Long id) {
        logger.info("Удаление Пользователя с ID: {}...", id);

        userDao.deleteById(id);
    }
}
