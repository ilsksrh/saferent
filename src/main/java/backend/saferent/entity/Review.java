package backend.saferent.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    // Optional per-category ratings (1–5), Airbnb-style
    @Min(1) @Max(5) private Short cleanliness;
    @Min(1) @Max(5) private Short accuracy;
    @Min(1) @Max(5) private Short checkin;
    @Min(1) @Max(5) private Short communication;
    @Min(1) @Max(5) private Short location;

    @Min(1) @Max(5)
    @Column(name = "rating_value")
    private Short value;
}