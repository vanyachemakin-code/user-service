package userService;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import userService.entity.User;
import userService.util.HibernateUtil;

public abstract class TestDB {

    protected static SessionFactory testSessionFactory;

    @BeforeAll
    static void beforeAll() {
        try {
            Configuration configuration = new Configuration();

            configuration.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
            configuration.setProperty("hibernate.connection.url", "jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
            configuration.setProperty("hibernate.connection.username", "test");
            configuration.setProperty("hibernate.connection.password", "test");
            configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            configuration.setProperty("hibernate.hbm2ddl.auto", "create-drop");
            configuration.setProperty("hibernate.show_sql", "false");

            configuration.addAnnotatedClass(User.class);

            testSessionFactory = configuration.buildSessionFactory();

            HibernateUtil.setSessionFactory(testSessionFactory);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось инициализировать тестовую базу данных H2", e);
        }
    }

    @BeforeEach
    void setUpBase() {
        try (Session session = testSessionFactory.openSession()) {
            session.beginTransaction();
            session.createMutationQuery("delete from User").executeUpdate();
            session.getTransaction().commit();
        }
    }

    @AfterAll
    static void afterAll() {
        if (testSessionFactory != null && !testSessionFactory.isClosed()) {
            testSessionFactory.close();
        }
    }
}
