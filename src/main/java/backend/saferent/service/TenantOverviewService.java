package backend.saferent.service;

import backend.saferent.dto.response.contract.TenantOverviewResponse;
import backend.saferent.entity.Contract;
import backend.saferent.entity.Payment;
import backend.saferent.entity.RentPeriod;
import backend.saferent.entity.User;
import backend.saferent.entity.enums.ContractStatus;
import backend.saferent.entity.enums.PaymentStatus;
import backend.saferent.entity.enums.PaymentType;
import backend.saferent.entity.enums.RentPeriodStatus;
import backend.saferent.repository.ContractRepository;
import backend.saferent.repository.PaymentRepository;
import backend.saferent.repository.RentPeriodRepository;
import backend.saferent.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantOverviewService {

    private final ContractRepository contractRepository;
    private final PaymentRepository paymentRepository;
    private final RentPeriodRepository rentPeriodRepository;
    private final SecurityUtils securityUtils;

    public TenantOverviewResponse getMyTenancy() {
        User user = securityUtils.getCurrentUser();

        Contract contract = contractRepository.findByTenant(user).stream()
                .filter(c -> c.getStatus() == ContractStatus.ACTIVE)
                .max(Comparator.comparing(c ->
                        c.getSignedAt() != null ? c.getSignedAt() : c.getCreatedAt(),
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElse(null);

        if (contract == null) {
            return TenantOverviewResponse.builder().active(false).build();
        }

        Payment deposit = paymentRepository
                .findByContractAndType(contract, PaymentType.DEPOSIT)
                .orElse(null);
        BigDecimal depositInEscrow =
                (deposit != null && deposit.getStatus() == PaymentStatus.PAID)
                        ? contract.getDepositAmount() : BigDecimal.ZERO;

        List<RentPeriod> periods =
                rentPeriodRepository.findByContractOrderByPeriodIndexAsc(contract);

        BigDecimal outstanding = periods.stream()
                .filter(p -> p.getStatus() != RentPeriodStatus.PAID)
                .map(RentPeriod::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate nextDue = periods.stream()
                .filter(p -> p.getStatus() != RentPeriodStatus.PAID)
                .map(RentPeriod::getDueDate)
                .filter(java.util.Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);

        return TenantOverviewResponse.builder()
                .active(true)
                .contractId(contract.getId())
                .apartmentId(contract.getApartment().getId())
                .apartmentTitle(contract.getApartment().getTitle())
                .apartmentAddress(contract.getApartment().getAddress())
                .rentAmount(contract.getRentAmount())
                .depositInEscrow(depositInEscrow)
                .outstandingRent(outstanding)
                .nextDueDate(nextDue)
                .contractStatus(contract.getStatus().name())
                .build();
    }
}
