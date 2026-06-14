package backend.saferent.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LandlordAnalyticsResponse {

    private BigDecimal totalIncome;
    private BigDecimal thisMonthIncome;
    private BigDecimal escrowHeld;
    private double occupancyRate;
    private int totalApartments;
    private int occupiedApartments;

    private List<MonthIncome> byMonth;
    private List<ApartmentIncome> byApartment;
    private List<DistrictIncome> byDistrict;
    private SourceBreakdown bySource;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MonthIncome {
        private String month;        // "YYYY-MM"
        private BigDecimal rent;
        private BigDecimal damage;
        private BigDecimal bonus;
        private BigDecimal total;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ApartmentIncome {
        private UUID apartmentId;
        private String title;
        private BigDecimal total;
        private boolean occupied;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DistrictIncome {
        private String district;
        private BigDecimal total;
        private BigDecimal avgPerApartment;
        private int apartmentCount;
        private double occupancyRate;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SourceBreakdown {
        private BigDecimal rent;
        private BigDecimal damage;
        private BigDecimal bonus;
    }
}
