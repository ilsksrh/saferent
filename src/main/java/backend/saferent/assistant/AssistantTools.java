package backend.saferent.assistant;

import backend.saferent.assistant.llm.LlmClient.ToolSpec;
import backend.saferent.dto.response.apartment.ApartmentResponse;
import backend.saferent.dto.response.booking.BookingResponse;
import backend.saferent.dto.response.contract.ContractResponse;
import backend.saferent.dto.response.district.DistrictResponse;
import backend.saferent.dto.response.payment.EscrowStatusResponse;
import backend.saferent.dto.response.payment.RentPeriodResponse;
import backend.saferent.entity.enums.ContractStatus;
import backend.saferent.entity.enums.RentPeriodStatus;
import backend.saferent.service.ApartmentService;
import backend.saferent.service.BookingService;
import backend.saferent.service.ContractService;
import backend.saferent.service.DistrictService;
import backend.saferent.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Инструменты ИИ-консьержа. Выполняются in-process под текущим
 * {@code SecurityContext} — методы {@code getMyContracts/getMyBookings} уже
 * фильтруют данные по залогиненному пользователю, поэтому ассистент видит только
 * его собственную информацию.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssistantTools {

    private final ApartmentService apartmentService;
    private final DistrictService districtService;
    private final BookingService bookingService;
    private final ContractService contractService;
    private final PaymentService paymentService;

    /** Результат инструмента: map для модели + (опц.) карточки квартир для фронта. */
    public record ToolOutcome(Map<String, Object> result, List<ApartmentResponse> apartments) {
        static ToolOutcome of(Map<String, Object> result) { return new ToolOutcome(result, null); }
    }

    // ---- декларации инструментов для модели ---------------------------------

    public List<ToolSpec> specs() {
        return List.of(
                new ToolSpec(
                        "searchApartments",
                        "Найти доступные к аренде квартиры по фильтрам (район, число комнат, цена, удобства). " +
                                "Используй, когда пользователь ищет жильё.",
                        Map.of("type", "object", "properties", Map.of(
                                "district", Map.of("type", "string",
                                        "description", "Название района, например Алмалинский"),
                                "rooms", Map.of("type", "integer",
                                        "description", "Точное число комнат"),
                                "maxPrice", Map.of("type", "number",
                                        "description", "Максимальная цена аренды в тенге за месяц"),
                                "minPrice", Map.of("type", "number",
                                        "description", "Минимальная цена аренды в тенге за месяц"),
                                "amenities", Map.of("type", "array", "items", Map.of("type", "string"),
                                        "description", "Желаемые удобства, например кондиционер, парковка, Wi-Fi")
                        ))
                ),
                new ToolSpec(
                        "explainEscrow",
                        "Объяснить, как работает защита платежей SafeRent: эскроу, депозит, e-договор, инспекция. " +
                                "Используй для вопросов «как это работает / безопасно ли».",
                        Map.of("type", "object", "properties", Map.of(
                                "topic", Map.of("type", "string",
                                        "description", "escrow | deposit | protection | contract | inspection")
                        ))
                ),
                new ToolSpec(
                        "getMyTenancyStatus",
                        "Получить статус текущего пользователя как арендатора: его брони, договоры, " +
                                "ближайший платёж по аренде и статус депозита. Используй для вопросов про " +
                                "«мои брони / договор / когда платить / мой депозит».",
                        Map.of()
                )
        );
    }

    // ---- диспетчер ----------------------------------------------------------

    public ToolOutcome execute(String name, Map<String, Object> args) {
        Map<String, Object> a = args != null ? args : Map.of();
        try {
            return switch (name) {
                case "searchApartments" -> searchApartments(a);
                case "explainEscrow" -> ToolOutcome.of(explainEscrow(a));
                case "getMyTenancyStatus" -> ToolOutcome.of(getMyTenancyStatus());
                default -> ToolOutcome.of(Map.of("error", "Неизвестный инструмент: " + name));
            };
        } catch (Exception e) {
            log.warn("Tool '{}' failed: {}", name, e.getMessage());
            return ToolOutcome.of(Map.of("error", "Не удалось выполнить запрос: " + e.getMessage()));
        }
    }

    // ---- 1. поиск квартир ---------------------------------------------------

    private ToolOutcome searchApartments(Map<String, Object> args) {
        String district = asString(args.get("district"));
        Integer rooms = asInteger(args.get("rooms"));
        BigDecimal maxPrice = asBigDecimal(args.get("maxPrice"));
        BigDecimal minPrice = asBigDecimal(args.get("minPrice"));
        List<String> amenities = asStringList(args.get("amenities"));

        // район: имя -> id (contains, ignore-case)
        final java.util.UUID districtId;
        if (district != null && !district.isBlank()) {
            String needle = district.toLowerCase();
            districtId = districtService.getAllDistricts().stream()
                    .filter(d -> d.getName() != null && d.getName().toLowerCase().contains(needle))
                    .map(DistrictResponse::getId)
                    .findFirst().orElse(null);
        } else {
            districtId = null;
        }

        List<ApartmentResponse> matches = apartmentService.getAllActive().stream()
                .filter(ap -> districtId == null || districtId.equals(ap.getDistrictId()))
                .filter(ap -> rooms == null || (ap.getRooms() != null && ap.getRooms() == rooms.shortValue()))
                .filter(ap -> maxPrice == null || (ap.getPrice() != null && ap.getPrice().compareTo(maxPrice) <= 0))
                .filter(ap -> minPrice == null || (ap.getPrice() != null && ap.getPrice().compareTo(minPrice) >= 0))
                .filter(ap -> amenities.isEmpty() || hasAllAmenities(ap, amenities))
                .sorted(Comparator.comparing(ApartmentResponse::getPrice,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(5)
                .toList();

        // компактная версия для модели + полные карточки для фронта
        List<Map<String, Object>> brief = matches.stream().map(ap -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", ap.getId());
            m.put("title", ap.getTitle());
            m.put("price", ap.getPrice());
            m.put("rooms", ap.getRooms());
            m.put("area", ap.getArea());
            m.put("address", ap.getAddress());
            m.put("amenities", ap.getAmenities());
            return m;
        }).toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("count", matches.size());
        result.put("apartments", brief);
        if (matches.isEmpty()) {
            result.put("note", "По заданным фильтрам ничего не найдено. Предложи смягчить критерии.");
        }
        return new ToolOutcome(result, matches);
    }

    private boolean hasAllAmenities(ApartmentResponse ap, List<String> wanted) {
        List<String> have = ap.getAmenities() == null ? List.of() : ap.getAmenities();
        return wanted.stream().allMatch(w -> {
            String needle = w.toLowerCase();
            return have.stream().anyMatch(h -> h != null && h.toLowerCase().contains(needle));
        });
    }

    // ---- 2. справка по защите платежей --------------------------------------

    private Map<String, Object> explainEscrow(Map<String, Object> args) {
        String topic = asString(args.get("topic"));
        Map<String, String> kb = new LinkedHashMap<>();
        kb.put("escrow", "Эскроу: депозит арендатора замораживается на нейтральном счёте SafeRent и не " +
                "уходит арендодателю напрямую. После выезда и инспекции админ решает, кому вернуть деньги.");
        kb.put("deposit", "Депозит: страховой платёж (по умолчанию = 1 месяц аренды). Хранится в эскроу. " +
                "Если ущерба нет — полностью возвращается арендатору на кошелёк; при ущербе часть/всё идёт арендодателю.");
        kb.put("protection", "SafeRent Protection: подтверждённая личность сторон, AI-анализ состояния квартиры " +
                "по фото, депозит в эскроу и юридический e-договор. Платить и общаться нужно только внутри SafeRent.");
        kb.put("contract", "E-договор: юридический договор аренды подписывается обеими сторонами электронно. " +
                "Договор становится активным только после подписи арендатора и арендодателя.");
        kb.put("inspection", "Инспекция: арендодатель загружает фото до заселения, арендатор подтверждает; при " +
                "выезде фото сравниваются AI (SSIM). Результат влияет на решение по возврату депозита.");

        Map<String, Object> result = new LinkedHashMap<>();
        if (topic != null && kb.containsKey(topic.toLowerCase())) {
            result.put("info", kb.get(topic.toLowerCase()));
        } else {
            result.put("info", kb);
        }
        return result;
    }

    // ---- 3. статус арендатора -----------------------------------------------

    private Map<String, Object> getMyTenancyStatus() {
        List<BookingResponse> bookings = bookingService.getMyBookings();
        List<ContractResponse> contracts = contractService.getMyContracts();

        List<Map<String, Object>> bookingList = bookings.stream().map(b -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("apartment", b.getApartmentTitle());
            m.put("status", b.getStatus());
            m.put("startDate", b.getStartDate());
            m.put("endDate", b.getEndDate());
            return m;
        }).toList();

        List<Map<String, Object>> contractList = new ArrayList<>();
        Map<String, Object> nextPayment = null;
        Map<String, Object> deposit = null;

        for (ContractResponse c : contracts) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("apartment", c.getApartmentTitle());
            m.put("status", c.getStatus());
            m.put("rent", c.getRentAmount());
            m.put("startDate", c.getStartDate());
            m.put("endDate", c.getEndDate());
            contractList.add(m);

            if (c.getStatus() == ContractStatus.ACTIVE && nextPayment == null) {
                // ближайший неоплаченный период аренды
                List<RentPeriodResponse> schedule = paymentService.getRentSchedule(c.getId());
                RentPeriodResponse next = schedule.stream()
                        .filter(p -> p.getStatus() != RentPeriodStatus.PAID)
                        .min(Comparator.comparing(RentPeriodResponse::getDueDate,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .orElse(null);
                if (next != null) {
                    nextPayment = new LinkedHashMap<>();
                    nextPayment.put("apartment", c.getApartmentTitle());
                    nextPayment.put("dueDate", next.getDueDate());
                    nextPayment.put("amount", next.getAmount());
                    nextPayment.put("status", next.getStatus());
                }
                // статус депозита
                EscrowStatusResponse escrow = paymentService.getEscrowStatus(c.getId());
                deposit = new LinkedHashMap<>();
                deposit.put("amount", escrow.getDepositAmount());
                deposit.put("paid", escrow.isDepositPaid());
                deposit.put("status", escrow.getDepositStatus());
                deposit.put("note", escrow.getEscrowNote());
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("bookings", bookingList);
        result.put("contracts", contractList);
        result.put("nextPayment", nextPayment);
        result.put("deposit", deposit);
        if (bookings.isEmpty() && contracts.isEmpty()) {
            result.put("note", "У пользователя пока нет броней и договоров.");
        }
        return result;
    }

    // ---- coercion -----------------------------------------------------------

    private static String asString(Object o) {
        return o == null ? null : o.toString();
    }

    private static Integer asInteger(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        try {
            return (int) Math.round(Double.parseDouble(o.toString().trim()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static BigDecimal asBigDecimal(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try {
            return new BigDecimal(o.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> asStringList(Object o) {
        if (o == null) return List.of();
        if (o instanceof List<?> list) {
            return list.stream().filter(java.util.Objects::nonNull).map(Object::toString).toList();
        }
        String s = o.toString().trim();
        if (s.isEmpty()) return List.of();
        return List.of(s.split("\\s*,\\s*"));
    }
}
