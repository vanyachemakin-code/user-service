package userService.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 75)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "age")
    private Integer age;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UserEntity(String name, String email, Integer age) {
        this.name = name;
        this.email = email;
        this.age = age;
    }

    @PrePersist
    protected void setCreatedAt() {
        this.createdAt = LocalDateTime.now();
    }
}
