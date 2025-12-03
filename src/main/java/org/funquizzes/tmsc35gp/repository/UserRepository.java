package org.funquizzes.tmsc35gp.repository;

import org.funquizzes.tmsc35gp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
