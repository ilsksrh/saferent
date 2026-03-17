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
public class Review extends AbstractEntity {

    @ManyToOne
    private Contract contract;

    @ManyToOne
    private User author;

    @ManyToOne
    private User targetUser;

    @Min(1)
    @Max(5)
    private Short rating;

    private String comment;
}