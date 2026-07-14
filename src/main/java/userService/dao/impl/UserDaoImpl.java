package userService.dao.impl;

import org.hibernate.PropertyValueException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import userService.dao.UserDao;
import userService.entity.User;
import userService.exception.UserAppException;
import userService.exception.UserValidationException;
import userService.exception.UserNotFoundException;
import userService.util.HibernateUtil;

import java.text.MessageFormat;
import java.util.List;

public class UserDaoImpl implements UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    public void save(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            try {
                transaction = session.beginTransaction();
                session.persist(user);
                transaction.commit();

                logger.info("Пользователь: {}, успешно сохранен в БД", user.getName());
            } catch (ConstraintViolationException e) {
                if (session.isOpen() && transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }

                String constraintName = e.getConstraintName();
                if ("23505".equals(e.getSQLState()) || (constraintName != null && constraintName.contains("email"))) {
                    throw new UserValidationException(
                            MessageFormat.format("Пользователь с email: {0}, уже зарегистрирован!", user.getEmail())
                    );
                } else {
                    throw new UserValidationException("Заполнение обязательных полей нарушено (передано null)!");
                }
            } catch (PropertyValueException e) {
                if (session.isOpen() && transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                throw new UserValidationException(
                        MessageFormat.format(
                                "Заполнение обязательных полей нарушено! Поле [{0}], не может быть null!",
                                e.getPropertyName()
                        )
                );
            } catch (Exception e) {
                if (session.isOpen() && transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                throw new UserAppException("Не удалось сохранить Пользователя в БД!", e);
            }
        }
    }

    @Override
    public User findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);
            if (user == null) {
                throw new UserNotFoundException(id);
            }
            return user;
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new UserAppException(
                    MessageFormat.format("Ошибка при поиске Пользователя по ID: {0}!", id),
                    e);
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from User", User.class).list();
        } catch (Exception e) {
            throw new UserAppException("Не удалось получить список всех Пользователей!", e);
        }
    }

    @Override
    public void update(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            try {
                transaction = session.beginTransaction();
                session.merge(user);
                transaction.commit();
                logger.info("Пользователь с ID: {}, успешно обновлен.", user.getId());
            } catch (Exception e) {
                if (session.isOpen() && transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                throw new UserAppException(
                        MessageFormat.format("Ошибка обновления данных Пользователя с ID: {0}!", user.getId()),
                        e);
            }
        }
    }

    @Override
    public void deleteById(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            try {
                transaction = session.beginTransaction();
                User user = session.get(User.class, id);
                if (user == null) {
                    throw new UserNotFoundException(id);
                }
                session.remove(user);
                transaction.commit();
            } catch (UserNotFoundException e) {
                if (session.isOpen() && transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            } catch (Exception e) {
                if (session.isOpen() && transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                throw new UserAppException(
                        MessageFormat.format("Ошибка при удалении Пользователя с ID: {0}!", id),
                        e);
            }
        }
    }
}