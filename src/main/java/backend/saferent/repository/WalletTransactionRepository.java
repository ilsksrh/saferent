package backend.saferent.repository;

import backend.saferent.entity.User;
import backend.saferent.entity.Wallet;
import backend.saferent.entity.WalletTransaction;
import backend.saferent.entity.enums.WalletTxnType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {

    List<WalletTransaction> findByWalletOrderByCreatedAtDesc(Wallet wallet);

    List<WalletTransaction> findByWallet_UserAndTypeIn(User user, Collection<WalletTxnType> types);
}
