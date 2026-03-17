package backend.saferent.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Favorite extends AbstractEntity {

    @ManyToOne
    private User user;

    @ManyToOne
    private Apartment apartment;
}