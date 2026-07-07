package dao.impl;

import entity.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dao.UserDao;
import util.HibernateUtil;

import java.util.List;

public class UserDaoImpl implements UserDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    public void save(User user) {
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();

            logger.info("Пользователь: {}, успешно сохранен в БД", user.getName());
        } catch (ConstraintViolationException e) {
            logger.error("Ошибка целостность БД (вероятные ошибки: null значение, нарушение уникальности данных)!", e);

            if (transaction != null && transaction.getStatus().canRollback()) {
                transaction.rollback();
            }
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при сохранении Пользователя: {}!", user.getName(), e);

            if (transaction != null && transaction.getStatus().canRollback()) {
                transaction.rollback();
            }
        }
    }

    @Override
    public User findById(Long id) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, id);
        } catch (Exception e) {
            logger.error("Ошибка поиска Пользователя с ID: {}!", id, e);
            return null;
        }
    }

    @Override
    public List<User> findAll() {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from User", User.class).list();
        } catch (Exception e) {
            logger.error("Ошибка поиска всех Пользователей в БД!");
            return List.of();
        }
    }

    @Override
    public void update(User user) {
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(user);
            transaction.commit();

            logger.info("Пользователь с ID: {}, успешно обновлен.", user.getId());
        } catch (ConstraintViolationException e) {
            logger.error("Ошибка целостность БД (вероятные ошибки: null значение, нарушение уникальности данных)!", e);

            if (transaction != null && transaction.getStatus().canRollback()) {
                transaction.rollback();
            }
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при обновлении Пользователя с ID: {}!", user.getId(), e);

            if (transaction != null && transaction.getStatus().canRollback()) {
                transaction.rollback();
            }
        }
    }

    @Override
    public void deleteById(Long id) {
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            logger.info("Поиск Пользователя с ID: {}, перед удалением из БД", id);
            User user = session.get(User.class, id);
            if (user != null) {
                session.remove(user);
                transaction.commit();

                logger.info("Пользователь с ID: {}, успешно удален из БД.", id);
            } else {
                logger.warn("Удаление невозможно, Пользователь с ID: {}, не найден!", id);
            }
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при удалении Пользователя: {}!", id, e);

            if (transaction != null && transaction.getStatus().canRollback()) {
                transaction.rollback();
            }
        }
    }
}
