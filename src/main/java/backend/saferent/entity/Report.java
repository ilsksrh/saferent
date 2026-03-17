package backend.saferent.entity;

import backend.saferent.entity.enums.ReportReason;
import backend.saferent.entity.enums.ReportStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report extends AbstractEntity {

    @ManyToOne
    private User reporter;

    @ManyToOne
    private User targetUser;

    @ManyToOne
    private Apartment apartment;

    @ManyToOne
    private Message message;

    @Enumerated(EnumType.STRING)
    private ReportReason reason;

    private String description;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    @ManyToOne
    private User moderator;

    private String resolutionComment;

    private java.time.LocalDateTime resolvedAt;
}