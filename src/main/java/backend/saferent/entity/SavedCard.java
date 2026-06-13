package backend.saferent.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedCard extends AbstractEntity {

    @ManyToOne(optional = false)
    private User user;

    /** Card brand derived from the number (VISA / Mastercard / CARD). */
    private String brand;

    /** Only the last 4 digits are ever stored — never the full PAN or CVV. */
    private String last4;

    private int expMonth;
    private int expYear;

    private boolean defaultCard;
}
