package backend.saferent.service;

import backend.saferent.dto.response.analytics.LandlordAnalyticsResponse;
import backend.saferent.dto.response.analytics.LandlordAnalyticsResponse.*;
import backend.saferent.entity.Apartment;
import backend.saferent.entity.Payment;
import backend.saferent.entity.User;
import backend.saferent.entity.WalletTransaction;
import backend.saferent.entity.enums.ApartmentStatus;
import backend.saferent.entity.enums.ContractStatus;
import backend.saferent.entity.enums.PaymentStatus;
import backend.saferent.entity.enums.PaymentType;
import backend.saferent.entity.enums.WalletTxnType;
import backend.saferent.repository.ApartmentRepository;
import backend.saferent.repository.ContractRepository;
import backend.saferent.repository.PaymentRepository;
import backend.saferent.repository.WalletTransactionRepository;
import backend.saferent.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LandlordAnalyticsService {

    private static final List<WalletTxnType> INCOME = List.of(
            WalletTxnType.RENT_INCOME, WalletTxnType.DAMAGE_COMPENSATION, WalletTxnType.BONUS);

    private final WalletTransactionRepository txnRepository;
    private final ApartmentRepository apartmentRepository;
    private final ContractRepository contractRepository;
    private final PaymentRepository paymentRepository;
    private final SecurityUtils securityUtils;

    public LandlordAnalyticsResponse getMyAnalytics() {
        User landlord = securityUtils.getCurrentUser();

        List<WalletTransaction> income =
                txnRepository.findByWallet_UserAndTypeIn(landlord, INCOME);

        BigDecimal total = sum(income);
        YearMonth nowYm = YearMonth.now();
        BigDecimal thisMonth = income.stream()
                .filter(t -> YearMonth.from(t.getCreatedAt()).equals(nowYm))
                .map(WalletTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal rent = sumType(income, WalletTxnType.RENT_INCOME);
        BigDecimal damage = sumType(income, WalletTxnType.DAMAGE_COMPENSATION);
        BigDecimal bonus = sumType(income, WalletTxnType.BONUS);

        // by month (sorted ascending)
        Map<String, List<WalletTransaction>> byMonthMap = income.stream()
                .collect(Collectors.groupingBy(t -> YearMonth.from(t.getCreatedAt()).toString()));
        List<MonthIncome> byMonth = byMonthMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    List<WalletTransaction> ts = e.getValue();
                    BigDecimal r = sumType(ts, WalletTxnType.RENT_INCOME);
                    BigDecimal d = sumType(ts, WalletTxnType.DAMAGE_COMPENSATION);
                    BigDecimal b = sumType(ts, WalletTxnType.BONUS);
                    return MonthIncome.builder().month(e.getKey())
                            .rent(r).damage(d).bonus(b).total(r.add(d).add(b)).build();
                })
                .collect(Collectors.toList());

        // landlord apartments (non-deleted)
        List<Apartment> apts = apartmentRepository.findAllByLandlordId(landlord.getId()).stream()
                .filter(a -> a.getDeletedAt() == null)
                .collect(Collectors.toList());

        Map<UUID, BigDecimal> incomeByApt = income.stream()
                .filter(t -> t.getApartment() != null)
                .collect(Collectors.groupingBy(t -> t.getApartment().getId(),
                        Collectors.reducing(BigDecimal.ZERO, WalletTransaction::getAmount, BigDecimal::add)));

        List<ApartmentIncome> byApartment = apts.stream()
                .map(a -> ApartmentIncome.builder()
                        .apartmentId(a.getId())
                        .title(a.getTitle())
                        .total(incomeByApt.getOrDefault(a.getId(), BigDecimal.ZERO))
                        .occupied(a.getStatus() == ApartmentStatus.RENTED)
                        .build())
                .collect(Collectors.toList());

        // by district — "where is it more profitable"
        Map<String, List<Apartment>> aptsByDistrict = apts.stream()
                .collect(Collectors.groupingBy(a -> a.getDistrict() != null ? a.getDistrict().getName() : "—"));
        List<DistrictIncome> byDistrict = aptsByDistrict.entrySet().stream()
                .map(e -> {
                    List<Apartment> da = e.getValue();
                    BigDecimal dtotal = da.stream()
                            .map(a -> incomeByApt.getOrDefault(a.getId(), BigDecimal.ZERO))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    long occ = da.stream().filter(a -> a.getStatus() == ApartmentStatus.RENTED).count();
                    int count = da.size();
                    BigDecimal avg = count > 0
                            ? dtotal.divide(BigDecimal.valueOf(count), 0, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return DistrictIncome.builder()
                            .district(e.getKey()).total(dtotal).avgPerApartment(avg)
                            .apartmentCount(count)
                            .occupancyRate(count > 0 ? round1(occ * 100.0 / count) : 0)
                            .build();
                })
                .sorted((x, y) -> y.getTotal().compareTo(x.getTotal()))
                .collect(Collectors.toList());

        int totalApts = apts.size();
        int occupiedApts = (int) apts.stream().filter(a -> a.getStatus() == ApartmentStatus.RENTED).count();
        double occupancyRate = totalApts > 0 ? round1(occupiedApts * 100.0 / totalApts) : 0;

        // deposits currently held in escrow for this landlord
        BigDecimal escrowHeld = contractRepository.findByLandlord(landlord).stream()
                .filter(c -> c.getStatus() == ContractStatus.ACTIVE)
                .map(c -> paymentRepository.findByContractAndType(c, PaymentType.DEPOSIT).orElse(null))
                .filter(p -> p != null && p.getStatus() == PaymentStatus.PAID)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return LandlordAnalyticsResponse.builder()
                .totalIncome(total)
                .thisMonthIncome(thisMonth)
                .escrowHeld(escrowHeld)
                .occupancyRate(occupancyRate)
                .totalApartments(totalApts)
                .occupiedApartments(occupiedApts)
                .byMonth(byMonth)
                .byApartment(byApartment)
                .byDistrict(byDistrict)
                .bySource(SourceBreakdown.builder().rent(rent).damage(damage).bonus(bonus).build())
                .build();
    }

    private BigDecimal sum(List<WalletTransaction> ts) {
        return ts.stream().map(WalletTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumType(List<WalletTransaction> ts, WalletTxnType type) {
        return ts.stream().filter(t -> t.getType() == type)
                .map(WalletTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
