package backend.saferent.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "district_rating", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"district_id", "user_id"})  // один пользователь — одна оценка района
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistrictRating extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Min(1)
    @Max(10)
    @Column(nullable = false)
    private Integer safetyRating;

    @Min(1)
    @Max(10)
    @Column(nullable = false)
    private Integer comfortRating;
}