package backend.saferent.repository;

import backend.saferent.entity.Apartment;
import backend.saferent.entity.Chat;
import backend.saferent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {

    // Найти существующий чат между двумя пользователями по квартире
    Optional<Chat> findByTenantAndLandlordAndApartment(
            User tenant, User landlord, Apartment apartment
    );

    // Все чаты пользователя (где он tenant или landlord)
    @Query("SELECT c FROM Chat c WHERE c.tenant = :user OR c.landlord = :user")
    List<Chat> findAllByUser(@Param("user") User user);
}