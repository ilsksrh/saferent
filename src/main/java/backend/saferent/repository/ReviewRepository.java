package backend.saferent.repository;

import backend.saferent.entity.Contract;
import backend.saferent.entity.Review;
import backend.saferent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByTargetUserOrderByCreatedAtDesc(User user);

    List<Review> findByAuthorOrderByCreatedAtDesc(User author);

    boolean existsByContractAndAuthor(Contract contract, User author);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.targetUser = :user")
    Double getAverageRatingForUser(@Param("user") User user);

    int countByTargetUser(User user);
}