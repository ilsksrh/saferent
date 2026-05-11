package backend.saferent.repository;

import backend.saferent.entity.Contract;
import backend.saferent.entity.Payment;
import backend.saferent.entity.enums.PaymentStatus;
import backend.saferent.entity.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByContractOrderByCreatedAtDesc(Contract contract);

    Optional<Payment> findByContractAndType(Contract contract, PaymentType type);

    boolean existsByContractAndTypeAndStatus(
            Contract contract, PaymentType type, PaymentStatus status
    );
}