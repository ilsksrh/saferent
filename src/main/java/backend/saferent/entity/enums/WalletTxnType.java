package backend.saferent.entity.enums;

public enum WalletTxnType {
    TOP_UP,
    RENT_PAYMENT,        // tenant debit
    RENT_INCOME,         // landlord credit
    DEPOSIT_REFUND,      // tenant credit
    DAMAGE_COMPENSATION, // landlord credit
    WITHDRAWAL,          // landlord debit
    BONUS                // platform credit
}
