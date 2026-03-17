package backend.saferent.repository;

import backend.saferent.entity.Payment;
import backend.saferent.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByContract(Contract contract);

}