package userService;

import userService.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import userService.service.UserService;
import userService.util.HibernateUtil;

import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final UserService userService = new UserService();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        logger.info("Приложение user-userService.service запущено.");

        try {
            while (true) {
                System.out.println("\n===== МЕНЮ УПРАВЛЕНИЯ =====");
                System.out.println("1. Создать Пользователя (Create)");
                System.out.println("2. Найти Пользователя по ID (Read)");
                System.out.println("3. Показать список всех Пользователей (Read All)");
                System.out.println("4. Обновить данные Пользователя (Update)");
                System.out.println("5. Удалить Пользователя (Delete)");
                System.out.println("0. Выход");
                System.out.print("Выберите действие -> ");

                String choice = scanner.nextLine().trim();
                try {
                    switch (choice) {
                        case "1" -> createUser();
                        case "2" -> findUserById();
                        case "3" -> findAllUsers();
                        case "4" -> updateUser();
                        case "5" -> deleteUser();
                        case "0" -> {
                            logger.info("Выход из приложения!");
                            return;
                        }
                        default -> logger.warn("Неверная команда меню: {}!", choice);
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Ошибка ввода! Введите цифру.");
                } catch (Exception e) {
                    logger.error("Непредвиденная ошибка при выполнении операции!", e);
                }
            }
        } finally {
            HibernateUtil.shutdown();
        }
    }

    private static void createUser() {
        System.out.print("Введите имя:");
        String name = scanner.nextLine();

        System.out.print("Введите email:");
        String email = scanner.nextLine();

        System.out.print("Введите возраст:");
        int age = 0;
        while (true) {
            try {
                age = Integer.parseInt(scanner.nextLine());
                if (age > 0) {
                    break;
                }

                logger.warn("Возраст не может быть меньше 1! Повторите ввод:");
            } catch (NumberFormatException e) {
                logger.warn("Ошибка формата! Пожалуйста введите возраст цифрами:");
            }
        }

        User user = new User(name, email, age);
        userService.save(user);
    }

    private static void findUserById() {
        System.out.print("Введите ID для поиска Пользователя:");
        Long id = readLongInput();

        User user = userService.findById(id);
        if (user != null) {
            logger.info("Пользователь с ID: {}, найден.", id);
            logger.info("Данные о Пользователе: {}", user);
        } else {
            logger.warn("Пользователь с ID: {}, не существует в системе!", id);
        }
    }

    private static void findAllUsers() {
        List<User> users = userService.findAll();
        if (users.isEmpty()) {
            logger.warn("Список Пользователей пуст!");
        } else {
            logger.info("Получен список из {} записей:", users.size());
            users.forEach(user -> logger.info("Данные о Пользователях: {}", user));
        }
    }

    private static void updateUser() {
        System.out.print("Введите ID Пользователя для обновления данных: ");
        Long id = readLongInput();

        User user = userService.findById(id);
        if (user == null) {
            logger.warn("Обновление отменено! Пользователь с ID: {}, не найден.", id);
            return;
        }

        System.out.print("Новое имя: \n(введите Enter для пропуска)");
        String name = scanner.nextLine();
        if (!name.isBlank()) {
            user.setName(name);
        }

        System.out.print("Новый email: \n(введите Enter для пропуска)");
        String email = scanner.nextLine();
        if (!email.isBlank()) {
            user.setEmail(email);
        }

        System.out.print("Новый возраст: \n(введите 0 для пропуска): ");
        int age = 0;
        while (true) {
            try {
                age = Integer.parseInt(scanner.nextLine().trim());
                if (age >= 0) {
                    break;
                }

                logger.warn("Возраст не может быть отрицательным! Повторите ввод:");
            } catch (NumberFormatException e) {
                logger.warn("Ошибка формата! Пожалуйста, введите число цифрами:");
            }
        }
        if (age > 0) {
            user.setAge(age);
        }

        userService.update(user);
    }

    private static void deleteUser() {
        System.out.print("Введите ID Пользователя для его удаления:");
        Long id = readLongInput();
        userService.deleteById(id);
    }

    private static Long readLongInput() {
        while (true) {
            try {
                long id = Long.parseLong(scanner.nextLine().trim());
                if (id > 0) {
                    return id;
                }
                logger.warn("ID Пользователя должен быть больше 0! Повторите ввод:");
            } catch (NumberFormatException e) {
                logger.warn("Ошибка формата! Пожалуйста, введите корректный ID Пользователя цифрами:");
            }
        }
    }
}
