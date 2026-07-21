package userService.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import userService.entity.UserEntity;


public interface UserDao extends JpaRepository<UserEntity, Long> {

    boolean existsByEmail(String email);
}
