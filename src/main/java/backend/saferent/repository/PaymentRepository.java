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

    // Все платежи по договору
    List<Payment> findByContractOrderByCreatedAtDesc(Contract contract);

    // Найти конкретный тип платежа по договору
    Optional<Payment> findByContractAndType(Contract contract, PaymentType type);

    // Проверить оплачен ли депозит
    boolean existsByContractAndTypeAndStatus(
            Contract contract, PaymentType type, PaymentStatus status
    );
}