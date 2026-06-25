package com.FaceRecogSystem.RecogSystem.repository;

import com.FaceRecogSystem.RecogSystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findBySubjectName(String subjectName);
}
