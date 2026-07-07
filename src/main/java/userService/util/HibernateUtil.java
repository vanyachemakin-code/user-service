package userService.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateUtil {

    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    private static SessionFactory sessionFactory;

    private static SessionFactory buildSessionFactory() {
        try {
            logger.info("Инициализация Hibernate...");

            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable e) {
            logger.error("Ошибка создания SessionFactory!", e);

            throw new ExceptionInInitializerError(e);
        }
    }

    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = buildSessionFactory();
        }
        return sessionFactory;
    }

    public static synchronized void setSessionFactory(SessionFactory sessionFactory) {
        HibernateUtil.sessionFactory = sessionFactory;
    }

    public static synchronized void shutdown() {
        logger.info("Инициализация закрытия Session Factory...");

        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();

            logger.info("SessionFactory успешно закрыта!");
        } else {
            logger.warn("SessionFactory не была закрыта в данном вызове (причина: null или уже закрыта)!");
        }
    }
}
