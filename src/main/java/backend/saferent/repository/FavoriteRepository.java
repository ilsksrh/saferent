package backend.saferent.repository;

import backend.saferent.entity.Apartment;
import backend.saferent.entity.Favorite;
import backend.saferent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    // Все избранные пользователя
    List<Favorite> findByUserOrderByCreatedAtDesc(User user);

    // Найти конкретную запись
    Optional<Favorite> findByUserAndApartment(User user, Apartment apartment);

    // Уже в избранном?
    boolean existsByUserAndApartment(User user, Apartment apartment);

    // Сколько раз добавили квартиру в избранное
    int countByApartment(Apartment apartment);
}