package backend.saferent.scheduler;

import backend.saferent.entity.Contract;
import backend.saferent.entity.RentPeriod;
import backend.saferent.entity.enums.NotificationType;
import backend.saferent.entity.enums.RentPeriodStatus;
import backend.saferent.repository.RentPeriodRepository;
import backend.saferent.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RentReminderScheduler {

    private final RentPeriodRepository rentPeriodRepository;
    private final NotificationService notificationService;

    /** Daily at 09:00 — remind the tenant 5 days before each rent due date. */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void sendDueReminders() {
        LocalDate target = LocalDate.now().plusDays(5);
        List<RentPeriod> due = rentPeriodRepository
                .findByStatusAndDueDateAndReminderSentAtIsNull(RentPeriodStatus.DUE, target);

        for (RentPeriod period : due) {
            Contract contract = period.getContract();
            notificationService.create(
                    contract.getTenant().getId(),
                    "Скоро оплата аренды",
                    "Через 5 дней (" + period.getDueDate() + ") нужно оплатить аренду " +
                            period.getAmount() + " KZT за " +
                            contract.getApartment().getTitle() + ".",
                    NotificationType.PAYMENT,
                    contract.getId(),
                    "CONTRACT"
            );
            period.setReminderSentAt(LocalDateTime.now());
            rentPeriodRepository.save(period);
        }
    }

    /** Daily after midnight — flag overdue (unpaid past due date) periods. */
    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void markOverdue() {
        List<RentPeriod> overdue = rentPeriodRepository
                .findByStatusAndDueDateBefore(RentPeriodStatus.DUE, LocalDate.now());
        for (RentPeriod period : overdue) {
            period.setStatus(RentPeriodStatus.OVERDUE);
            rentPeriodRepository.save(period);
        }
    }
}
