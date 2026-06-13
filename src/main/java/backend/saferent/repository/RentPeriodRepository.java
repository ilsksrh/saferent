package backend.saferent.repository;

import backend.saferent.entity.Contract;
import backend.saferent.entity.RentPeriod;
import backend.saferent.entity.enums.RentPeriodStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RentPeriodRepository extends JpaRepository<RentPeriod, UUID> {

    List<RentPeriod> findByContractOrderByPeriodIndexAsc(Contract contract);

    Optional<RentPeriod> findFirstByContractAndStatusNotOrderByPeriodIndexAsc(
            Contract contract, RentPeriodStatus status);

    List<RentPeriod> findByStatusAndDueDateAndReminderSentAtIsNull(
            RentPeriodStatus status, LocalDate dueDate);

    List<RentPeriod> findByStatusAndDueDateBefore(
            RentPeriodStatus status, LocalDate date);
}
