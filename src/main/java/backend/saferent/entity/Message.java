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

    @NotBlank
    private String text;

    private Boolean isRead;
}