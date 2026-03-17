package backend.saferent.repository;


import backend.saferent.entity.Review;
import backend.saferent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByTargetUser(User user);

}