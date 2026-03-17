package backend.saferent.entity;

import backend.saferent.entity.enums.NotificationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends AbstractEntity {

    @ManyToOne
    private User user;

    @NotBlank
    @Size(max = 120)
    private String title;

    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private java.util.UUID relatedEntityId;

    private String relatedEntityType;

    private Boolean isRead;
}