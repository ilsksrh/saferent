package backend.saferent.repository;

import backend.saferent.entity.User;
import backend.saferent.entity.enums.PreferredRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByPhone(String phone);
    boolean existsByPhone(String phone);
    Optional<User> findByEgovId(String egovId);

    List<User> findAllByVerifiedTrue();
    List<User> findAllByPreferredRole(PreferredRole role);

    List<User> findByLastLoginAtBefore(LocalDateTime date);

    List<User> findByNameContainingIgnoreCase(String namePart);
}