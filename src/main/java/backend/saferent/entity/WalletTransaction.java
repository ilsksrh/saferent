package backend.saferent.entity;

import backend.saferent.entity.enums.WalletTxnType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransaction extends AbstractEntity {

    @ManyToOne(optional = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    private WalletTxnType type;

    /** Signed: positive = credit (in), negative = debit (out). */
    @Column(precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(precision = 14, scale = 2)
    private BigDecimal balanceAfter;

    private String description;

    @ManyToOne
    private Contract contract;

    @ManyToOne
    private Apartment apartment;
}
