package backend.saferent.service;

import backend.saferent.entity.Contract;
import backend.saferent.entity.Payment;
import backend.saferent.entity.RentPeriod;
import backend.saferent.entity.enums.RentPeriodStatus;
import backend.saferent.exception.BadRequestException;
import backend.saferent.repository.RentPeriodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentScheduleService {

    private final RentPeriodRepository rentPeriodRepository;

    /** Build a monthly rent schedule for the contract if it has none yet. */
    @Transactional
    public List<RentPeriod> generateIfAbsent(Contract contract) {
        List<RentPeriod> existing =
                rentPeriodRepository.findByContractOrderByPeriodIndexAsc(contract);
        if (!existing.isEmpty()) {
            return existing;
        }
        if (contract.getStartDate() == null || contract.getEndDate() == null) {
            return existing;
        }

        LocalDate start = contract.getStartDate();
        LocalDate end = contract.getEndDate();
        long months = ChronoUnit.MONTHS.between(start, end);
        if (months < 1) {
            months = 1;
        }

        List<RentPeriod> periods = new ArrayList<>();
        for (int i = 0; i < months; i++) {
            LocalDate periodStart = start.plusMonths(i);
            LocalDate periodEnd = (i == months - 1)
                    ? end
                    : start.plusMonths(i + 1).minusDays(1);

            periods.add(RentPeriod.builder()
                    .contract(contract)
                    .periodIndex(i + 1)
                    .periodStart(periodStart)
                    .periodEnd(periodEnd)
                    .dueDate(periodStart)
                    .amount(contract.getRentAmount())
                    .status(RentPeriodStatus.DUE)
                    .build());
        }
        return rentPeriodRepository.saveAll(periods);
    }

    public List<RentPeriod> getSchedule(Contract contract) {
        return generateIfAbsent(contract);
    }

    /** Mark the earliest unpaid period as paid; throws if everything is already paid. */
    @Transactional
    public RentPeriod markEarliestPaid(Contract contract, Payment payment) {
        generateIfAbsent(contract);
        RentPeriod period = rentPeriodRepository
                .findFirstByContractAndStatusNotOrderByPeriodIndexAsc(
                        contract, RentPeriodStatus.PAID)
                .orElseThrow(() -> new BadRequestException(
                        "All rent periods for this contract are already paid"));

        period.setStatus(RentPeriodStatus.PAID);
        period.setPayment(payment);
        period.setPaidAt(LocalDateTime.now());
        return rentPeriodRepository.save(period);
    }
}
