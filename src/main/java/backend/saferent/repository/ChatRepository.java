package backend.saferent.repository;

import backend.saferent.entity.Chat;
import backend.saferent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {

    List<Chat> findByTenantOrLandlord(User tenant, User landlord);

}