package userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import userservice.entity.UserEntity;


public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
}
