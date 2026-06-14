package backend.saferent.config;

import backend.saferent.entity.*;
import backend.saferent.entity.enums.*;
import backend.saferent.repository.*;
import backend.saferent.search.ApartmentSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DistrictRepository districtRepository;
    private final DistrictRatingRepository districtRatingRepository;
    private final ApartmentRepository apartmentRepository;
    private final ApartmentPhotoRepository apartmentPhotoRepository;
    private final FavoriteRepository favoriteRepository;
    private final BookingRepository bookingRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final ContractRepository contractRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationRepository notificationRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApartmentSearchService apartmentSearchService;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("DataLoader: test data already present, skipping seed");
            apartmentSearchService.reindexAll();
            return;
        }
        log.info("DataLoader: seeding test data...");

        // === DISTRICTS (Алматы) ===
        District medeu = districtRepository.save(District.builder()
                .name("Медеуский").safetyScore(9).comfortScore(9).colorCode("#4CAF50").build());
        District bostandyk = districtRepository.save(District.builder()
                .name("Бостандыкский").safetyScore(8).comfortScore(8).colorCode("#8BC34A").build());
        District almalin = districtRepository.save(District.builder()
                .name("Алмалинский").safetyScore(7).comfortScore(8).colorCode("#FFEB3B").build());
        District auezov = districtRepository.save(District.builder()
                .name("Ауэзовский").safetyScore(6).comfortScore(6).colorCode("#FF9800").build());
        District turksib = districtRepository.save(District.builder()
                .name("Турксибский").safetyScore(5).comfortScore(5).colorCode("#FF5722").build());

        // === USERS (пароль для всех: password123) ===
        String pwd = passwordEncoder.encode("password123");

        User admin = userRepository.save(User.builder()
                .phone("+77000000000").email("admin@saferent.kz").name("Admin SafeRent")
                .preferredRole(PreferredRole.LANDLORD).verified(true).admin(true)
                .passwordHash(pwd).build());

        User landlord1 = userRepository.save(User.builder()
                .phone("+77011111111").email("aigerim@example.com").name("Айгерим Нурланова")
                .preferredRole(PreferredRole.LANDLORD).verified(true).passwordHash(pwd)
                .lastLoginAt(LocalDateTime.now().minusDays(1)).build());

        User landlord2 = userRepository.save(User.builder()
                .phone("+77022222222").email("askar@example.com").name("Аскар Бекенов")
                .preferredRole(PreferredRole.LANDLORD).verified(true).passwordHash(pwd)
                .lastLoginAt(LocalDateTime.now().minusDays(2)).build());

        User landlord3 = userRepository.save(User.builder()
                .phone("+77033333333").email("dana@example.com").name("Дана Сериковна")
                .preferredRole(PreferredRole.LANDLORD).verified(false).passwordHash(pwd)
                .build());

        User tenant1 = userRepository.save(User.builder()
                .phone("+77044444444").email("ilesbek@example.com").name("Илесбек Сара")
                .preferredRole(PreferredRole.TENANT).verified(true).passwordHash(pwd)
                .lastLoginAt(LocalDateTime.now()).build());

        User tenant2 = userRepository.save(User.builder()
                .phone("+77055555555").email("yerlan@example.com").name("Ерлан Куаныш")
                .preferredRole(PreferredRole.TENANT).verified(true).passwordHash(pwd)
                .build());

        User tenant3 = userRepository.save(User.builder()
                .phone("+77066666666").email("aruzhan@example.com").name("Аружан Маратовна")
                .preferredRole(PreferredRole.TENANT).verified(false).passwordHash(pwd)
                .build());

        // === DISTRICT RATINGS ===
        districtRatingRepository.save(DistrictRating.builder()
                .district(medeu).user(tenant1).safetyRating(9).comfortRating(10).build());
        districtRatingRepository.save(DistrictRating.builder()
                .district(medeu).user(tenant2).safetyRating(10).comfortRating(9).build());
        districtRatingRepository.save(DistrictRating.builder()
                .district(bostandyk).user(tenant1).safetyRating(8).comfortRating(8).build());
        districtRatingRepository.save(DistrictRating.builder()
                .district(auezov).user(tenant2).safetyRating(6).comfortRating(5).build());

        // === APARTMENTS ===
        Apartment apt1 = apartmentRepository.save(Apartment.builder()
                .landlord(landlord1).district(medeu)
                .title("Уютная 2-комнатная у парка Медеу")
                .description("Светлая квартира с панорамным видом на горы. Полностью меблирована, новая техника, тёплый пол. Рядом фуникулёр и каток Медеу.")
                .address("мкр. Самал-2, д. 75")
                .price(new BigDecimal("280000")).area(new BigDecimal("65.50")).rooms((short) 2)
                .availableFrom(LocalDate.now().plusDays(7))
                .verified(true).status(ApartmentStatus.ACTIVE).build());

        Apartment apt2 = apartmentRepository.save(Apartment.builder()
                .landlord(landlord1).district(bostandyk)
                .title("Современная студия у Mega")
                .description("Стильная студия в новом ЖК. Со всей бытовой техникой, кондиционер, тихий двор. 5 минут до ТРЦ Mega.")
                .address("ул. Розыбакиева, 247")
                .price(new BigDecimal("180000")).area(new BigDecimal("38.00")).rooms((short) 1)
                .availableFrom(LocalDate.now())
                .verified(true).status(ApartmentStatus.ACTIVE).build());

        Apartment apt3 = apartmentRepository.save(Apartment.builder()
                .landlord(landlord2).district(almalin)
                .title("3-комнатная в центре, ремонт 2024")
                .description("Просторная квартира в кирпичном доме. Свежий ремонт, новая сантехника, две лоджии. Рядом метро Алмалы.")
                .address("ул. Толе би, 187")
                .price(new BigDecimal("350000")).area(new BigDecimal("92.00")).rooms((short) 3)
                .availableFrom(LocalDate.now().plusDays(14))
                .verified(true).status(ApartmentStatus.ACTIVE).build());

        Apartment apt4 = apartmentRepository.save(Apartment.builder()
                .landlord(landlord2).district(auezov)
                .title("Бюджетная 1-комнатная для студентов")
                .description("Уютная квартира недалеко от КазНПУ. Базовая мебель, всё необходимое для проживания. Развитая инфраструктура.")
                .address("мкр. Аксай-3А, д. 12")
                .price(new BigDecimal("120000")).area(new BigDecimal("36.00")).rooms((short) 1)
                .availableFrom(LocalDate.now().plusDays(3))
                .verified(false).status(ApartmentStatus.ACTIVE).build());

        Apartment apt5 = apartmentRepository.save(Apartment.builder()
                .landlord(landlord3).district(turksib)
                .title("Квартира рядом с вокзалом")
                .description("2-комнатная квартира недалеко от вокзала Алматы-1. Мебель и техника. Хороший вариант для командированных.")
                .address("ул. Ауэзова, 14")
                .price(new BigDecimal("150000")).area(new BigDecimal("48.00")).rooms((short) 2)
                .availableFrom(LocalDate.now())
                .verified(false).status(ApartmentStatus.ACTIVE).build());

        Apartment apt6 = apartmentRepository.save(Apartment.builder()
                .landlord(landlord1).district(medeu)
                .title("Премиум 4-комнатная с террасой")
                .description("Эксклюзивное жильё в Горном Гиганте. Дизайнерский ремонт, камин, терраса 30 м². Закрытая территория, охрана.")
                .address("Горный Гигант, 25")
                .price(new BigDecimal("750000")).area(new BigDecimal("145.00")).rooms((short) 4)
                .availableFrom(LocalDate.now().plusDays(30))
                .verified(true).status(ApartmentStatus.RENTED).build());

        // === APARTMENT PHOTOS (используем placeholder URL) ===
        String ph = "https://picsum.photos/seed/";
        for (var pair : List.of(
                new Object[]{apt1, 1, "apt1a"}, new Object[]{apt1, 2, "apt1b"}, new Object[]{apt1, 3, "apt1c"},
                new Object[]{apt2, 1, "apt2a"}, new Object[]{apt2, 2, "apt2b"},
                new Object[]{apt3, 1, "apt3a"}, new Object[]{apt3, 2, "apt3b"}, new Object[]{apt3, 3, "apt3c"},
                new Object[]{apt4, 1, "apt4a"},
                new Object[]{apt5, 1, "apt5a"}, new Object[]{apt5, 2, "apt5b"},
                new Object[]{apt6, 1, "apt6a"}, new Object[]{apt6, 2, "apt6b"}
        )) {
            apartmentPhotoRepository.save(ApartmentPhoto.builder()
                    .apartment((Apartment) pair[0])
                    .position(((Integer) pair[1]).shortValue())
                    .url(ph + pair[2] + "/800/600")
                    .build());
        }

        // === FAVORITES ===
        favoriteRepository.save(Favorite.builder().user(tenant1).apartment(apt1).build());
        favoriteRepository.save(Favorite.builder().user(tenant1).apartment(apt3).build());
        favoriteRepository.save(Favorite.builder().user(tenant2).apartment(apt2).build());
        favoriteRepository.save(Favorite.builder().user(tenant2).apartment(apt6).build());

        // === BOOKINGS ===
        bookingRepository.save(Booking.builder()
                .apartment(apt1).tenant(tenant1)
                .startDate(LocalDate.now().plusDays(10)).endDate(LocalDate.now().plusMonths(6))
                .message("Здравствуйте! Хочу снять на 6 месяцев, въезд в течение недели.")
                .status(BookingStatus.PENDING).build());

        bookingRepository.save(Booking.builder()
                .apartment(apt2).tenant(tenant2)
                .startDate(LocalDate.now().plusDays(5)).endDate(LocalDate.now().plusMonths(12))
                .message("Готов оплатить депозит сразу.")
                .status(BookingStatus.APPROVED).build());

        bookingRepository.save(Booking.builder()
                .apartment(apt4).tenant(tenant3)
                .startDate(LocalDate.now().plusDays(15)).endDate(LocalDate.now().plusMonths(9))
                .status(BookingStatus.REJECTED).build());

        // === CHATS + MESSAGES ===
        Chat chat1 = chatRepository.save(Chat.builder()
                .tenant(tenant1).landlord(landlord1).apartment(apt1).build());
        messageRepository.save(Message.builder().chat(chat1).sender(tenant1)
                .text("Здравствуйте, квартира ещё доступна?").isRead(true).build());
        messageRepository.save(Message.builder().chat(chat1).sender(landlord1)
                .text("Да, добрый день! Можем встретиться завтра в 16:00?").isRead(true).build());
        messageRepository.save(Message.builder().chat(chat1).sender(tenant1)
                .text("Отлично, до встречи!").isRead(false).build());

        Chat chat2 = chatRepository.save(Chat.builder()
                .tenant(tenant2).landlord(landlord1).apartment(apt2).build());
        messageRepository.save(Message.builder().chat(chat2).sender(tenant2)
                .text("Можно посмотреть квартиру?").isRead(true).build());
        messageRepository.save(Message.builder().chat(chat2).sender(landlord1)
                .text("Конечно, когда удобно?").isRead(false).build());

        // === CONTRACT (для apt2 - APPROVED booking) ===
        Contract contract1 = contractRepository.save(Contract.builder()
                .tenant(tenant2).landlord(landlord1).apartment(apt2)
                .startDate(LocalDate.now().plusDays(5)).endDate(LocalDate.now().plusMonths(12))
                .rentAmount(new BigDecimal("180000")).depositAmount(new BigDecimal("180000"))
                .status(ContractStatus.ACTIVE)
                .terms("Стандартный договор аренды. Депозит возвращается при сдаче без повреждений.")
                .signedAt(LocalDateTime.now().minusDays(1))
                .build());

        // === PAYMENTS ===
        paymentRepository.save(Payment.builder()
                .contract(contract1).type(PaymentType.DEPOSIT).amount(new BigDecimal("180000"))
                .method(PaymentMethod.KASPI).status(PaymentStatus.PAID)
                .transactionId("KASPI-TX-" + System.currentTimeMillis())
                .paidAt(LocalDateTime.now().minusDays(1)).build());

        paymentRepository.save(Payment.builder()
                .contract(contract1).type(PaymentType.RENT).amount(new BigDecimal("180000"))
                .method(PaymentMethod.HALYK).status(PaymentStatus.PENDING).build());

        // === REVIEWS ===
        reviewRepository.save(Review.builder()
                .contract(contract1).author(tenant2).targetUser(landlord1)
                .rating((short) 5).comment("Отличный хозяин, очень отзывчивый. Квартира как на фото.").build());

        // === NOTIFICATIONS ===
        notificationRepository.save(Notification.builder().user(landlord1)
                .title("Новая заявка").message("Айгерим хочет арендовать вашу квартиру 'Уютная 2-комнатная у парка Медеу'")
                .type(NotificationType.BOOKING).isRead(false).build());

        notificationRepository.save(Notification.builder().user(tenant2)
                .title("Заявка одобрена").message("Ваша заявка на 'Современная студия у Mega' одобрена")
                .type(NotificationType.BOOKING).isRead(true).build());

        notificationRepository.save(Notification.builder().user(tenant1)
                .title("Новое сообщение").message("Аскар Бекенов: 'Конечно, когда удобно?'")
                .type(NotificationType.MESSAGE).isRead(false).build());

        // === WALLETS + LEDGER (демо-данные для дашборда лэндлорда) ===
        Wallet w1 = wallet(landlord1);
        for (int m = 4; m >= 0; m--) {
            LocalDateTime when = LocalDateTime.now().minusMonths(m).withDayOfMonth(5).withHour(10);
            seedTxn(w1, WalletTxnType.RENT_INCOME, apt2.getPrice(), true, contract1, apt2,
                    "Доход от аренды: " + apt2.getTitle(), when);
            seedTxn(w1, WalletTxnType.RENT_INCOME, apt6.getPrice(), true, null, apt6,
                    "Доход от аренды: " + apt6.getTitle(), when.plusDays(1));
        }
        seedTxn(w1, WalletTxnType.DAMAGE_COMPENSATION, new BigDecimal("90000"), true, null, apt1,
                "Компенсация за ущерб: " + apt1.getTitle(),
                LocalDateTime.now().minusMonths(1).withDayOfMonth(20));
        seedTxn(w1, WalletTxnType.BONUS, new BigDecimal("25000"), true, null, null,
                "Superhost бонус", LocalDateTime.now().minusDays(2));

        seedTxn(wallet(tenant1), WalletTxnType.TOP_UP, new BigDecimal("600000"), true, null, null,
                "Пополнение (Kaspi)", LocalDateTime.now().minusDays(3));
        seedTxn(wallet(tenant2), WalletTxnType.TOP_UP, new BigDecimal("400000"), true, null, null,
                "Пополнение (Kaspi)", LocalDateTime.now().minusDays(2));

        log.info("DataLoader: seeded {} users, {} districts, {} apartments",
                userRepository.count(), districtRepository.count(), apartmentRepository.count());
        log.info("DataLoader: TEST CREDENTIALS — admin: +77000000000, landlord: +77011111111, tenant: +77044444444 (password: password123)");

        apartmentSearchService.reindexAll();
    }

    private Wallet wallet(User u) {
        return walletRepository.findByUser(u).orElseGet(() ->
                walletRepository.save(Wallet.builder().user(u).balance(BigDecimal.ZERO).build()));
    }

    private void seedTxn(Wallet w, WalletTxnType type, BigDecimal absAmount, boolean credit,
                         Contract c, Apartment a, String desc, LocalDateTime when) {
        BigDecimal signed = credit ? absAmount : absAmount.negate();
        w.setBalance(w.getBalance().add(signed));
        walletRepository.save(w);
        WalletTransaction t = walletTransactionRepository.save(WalletTransaction.builder()
                .wallet(w).type(type).amount(signed).balanceAfter(w.getBalance())
                .contract(c).apartment(a).description(desc).build());
        t.setCreatedAt(when);
        walletTransactionRepository.save(t);
    }
}
