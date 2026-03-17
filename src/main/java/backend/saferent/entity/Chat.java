package backend.saferent.entity;


import jakarta.persistence.*;

import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chat extends AbstractEntity {

    @ManyToOne
    private User tenant;

    @ManyToOne
    private User landlord;

    @ManyToOne
    private Apartment apartment;
}