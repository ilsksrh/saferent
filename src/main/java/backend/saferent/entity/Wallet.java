package backend.saferent.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet extends AbstractEntity {

    @OneToOne(optional = false)
    private User user;

    @Column(precision = 14, scale = 2)
    private BigDecimal balance;
}
