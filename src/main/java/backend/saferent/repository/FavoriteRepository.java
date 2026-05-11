package backend.saferent.repository;

import backend.saferent.entity.Apartment;
import backend.saferent.entity.Favorite;
import backend.saferent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    List<Favorite> findByUserOrderByCreatedAtDesc(User user);

    Optional<Favorite> findByUserAndApartment(User user, Apartment apartment);

    boolean existsByUserAndApartment(User user, Apartment apartment);

    int countByApartment(Apartment apartment);
}