package backend.saferent.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message extends AbstractEntity {

    @ManyToOne
    private Chat chat;

    @ManyToOne
    private User sender;

    @Column(columnDefinition = "TEXT")
    private String text;

    @Column(length = 1024)
    private String imageUrl;

    private Boolean isRead;
}