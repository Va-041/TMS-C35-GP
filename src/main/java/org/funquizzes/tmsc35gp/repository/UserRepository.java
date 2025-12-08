package org.funquizzes.tmsc35gp.repository;

import org.funquizzes.tmsc35gp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(String name, String username);

    @Query("SELECT u FROM User u WHERE u.isPublicProfile = true")
    List<User> findPublicUsers();

}
