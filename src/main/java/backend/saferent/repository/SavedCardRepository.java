package backend.saferent.repository;

import backend.saferent.entity.SavedCard;
import backend.saferent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SavedCardRepository extends JpaRepository<SavedCard, UUID> {

    List<SavedCard> findByUserOrderByCreatedAtDesc(User user);

    List<SavedCard> findByUserAndDefaultCardTrue(User user);
}
