package userService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import userService.entity.UserEntity;


public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByEmail(String email);
}
