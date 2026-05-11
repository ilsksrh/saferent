package backend.saferent.repository;

import backend.saferent.entity.Chat;
import backend.saferent.entity.Message;
import backend.saferent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findByChatOrderByCreatedAtAsc(Chat chat);

    Optional<Message> findTopByChatOrderByCreatedAtDesc(Chat chat);

    @Query("SELECT m FROM Message m WHERE m.chat = :chat " +
            "AND m.sender <> :user AND m.isRead = false")
    List<Message> findUnreadMessages(
            @Param("chat") Chat chat,
            @Param("user") User user
    );

    @Query("SELECT COUNT(m) FROM Message m " +
            "WHERE (m.chat.tenant = :user OR m.chat.landlord = :user) " +
            "AND m.sender <> :user AND m.isRead = false")
    int countUnreadForUser(@Param("user") User user);
}