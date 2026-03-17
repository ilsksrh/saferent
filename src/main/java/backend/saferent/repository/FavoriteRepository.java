package backend.saferent.repository;

import backend.saferent.entity.Favorite;
import backend.saferent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    List<Favorite> findByUser(User user);

}