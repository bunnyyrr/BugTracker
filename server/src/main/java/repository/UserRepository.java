package repository;

import com.bugtracker.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameIgnoreCase(String username);
    Optional<User> findByEmailIgnoreCase(String email);

    @Query("select u from User where lower(u.username)= lower(:login) or lower(u.email)= lower(:login)")
    Optional<User> findByLogin(@Param("login") String login);
}
