package backend.saferent.service;

import backend.saferent.dto.response.wallet.WalletResponse;
import backend.saferent.dto.response.wallet.WalletTransactionResponse;
import backend.saferent.entity.Apartment;
import backend.saferent.entity.Contract;
import backend.saferent.entity.User;
import backend.saferent.entity.Wallet;
import backend.saferent.entity.WalletTransaction;
import backend.saferent.entity.enums.WalletTxnType;
import backend.saferent.exception.BadRequestException;
import backend.saferent.repository.WalletRepository;
import backend.saferent.repository.WalletTransactionRepository;
import backend.saferent.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository txnRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public Wallet getOrCreateWallet(User user) {
        return walletRepository.findByUser(user)
                .orElseGet(() -> walletRepository.save(
                        Wallet.builder().user(user).balance(BigDecimal.ZERO).build()));
    }

    public BigDecimal balanceOf(User user) {
        return getOrCreateWallet(user).getBalance();
    }

    /** Core ledger primitive. signedAmount: positive = credit, negative = debit. */
    @Transactional
    public WalletTransaction post(User user, WalletTxnType type, BigDecimal signedAmount,
                                  Contract contract, Apartment apartment, String description) {
        Wallet w = getOrCreateWallet(user);
        BigDecimal newBalance = w.getBalance().add(signedAmount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Недостаточно средств на кошельке");
        }
        w.setBalance(newBalance);
        walletRepository.save(w);
        return txnRepository.save(WalletTransaction.builder()
                .wallet(w)
                .type(type)
                .amount(signedAmount)
                .balanceAfter(newBalance)
                .contract(contract)
                .apartment(apartment)
                .description(description)
                .build());
    }

    // ─── public API ──────────────────────────────────────────────────────

    public WalletResponse getMyWallet() {
        User user = securityUtils.getCurrentUser();
        Wallet w = getOrCreateWallet(user);
        List<WalletTransactionResponse> recent = txnRepository
                .findByWalletOrderByCreatedAtDesc(w)
                .stream().limit(10).map(this::toResponse).collect(Collectors.toList());
        return WalletResponse.builder().balance(w.getBalance()).recent(recent).build();
    }

    public List<WalletTransactionResponse> getMyTransactions() {
        User user = securityUtils.getCurrentUser();
        Wallet w = getOrCreateWallet(user);
        return txnRepository.findByWalletOrderByCreatedAtDesc(w)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public WalletResponse topUp(BigDecimal amount, String method) {
        User user = securityUtils.getCurrentUser();
        post(user, WalletTxnType.TOP_UP, amount.abs(), null, null,
                "Пополнение" + (method != null && !method.isBlank() ? " (" + method + ")" : ""));
        return getMyWallet();
    }

    @Transactional
    public WalletResponse withdraw(BigDecimal amount) {
        User user = securityUtils.getCurrentUser();
        post(user, WalletTxnType.WITHDRAWAL, amount.abs().negate(), null, null, "Вывод средств");
        return getMyWallet();
    }

    @Transactional
    public void grantBonus(User user, BigDecimal amount, String reason) {
        post(user, WalletTxnType.BONUS, amount.abs(), null, null,
                reason != null && !reason.isBlank() ? reason : "Бонус платформы");
    }

    // ─── payment hooks ───────────────────────────────────────────────────

    @Transactional
    public void chargeTenantForRent(Contract c, BigDecimal amount) {
        post(c.getTenant(), WalletTxnType.RENT_PAYMENT, amount.abs().negate(), c, c.getApartment(),
                "Оплата аренды: " + c.getApartment().getTitle());
    }

    @Transactional
    public void recordRentIncome(Contract c, BigDecimal amount) {
        post(c.getLandlord(), WalletTxnType.RENT_INCOME, amount.abs(), c, c.getApartment(),
                "Доход от аренды: " + c.getApartment().getTitle());
    }

    @Transactional
    public void refundDepositToTenant(Contract c) {
        if (c.getDepositAmount() == null || c.getDepositAmount().signum() == 0) return;
        post(c.getTenant(), WalletTxnType.DEPOSIT_REFUND, c.getDepositAmount().abs(), c, c.getApartment(),
                "Возврат залога: " + c.getApartment().getTitle());
    }

    @Transactional
    public void compensateLandlord(Contract c) {
        if (c.getDepositAmount() == null || c.getDepositAmount().signum() == 0) return;
        post(c.getLandlord(), WalletTxnType.DAMAGE_COMPENSATION, c.getDepositAmount().abs(), c, c.getApartment(),
                "Компенсация за ущерб: " + c.getApartment().getTitle());
    }

    private WalletTransactionResponse toResponse(WalletTransaction t) {
        return WalletTransactionResponse.builder()
                .id(t.getId())
                .type(t.getType())
                .amount(t.getAmount())
                .balanceAfter(t.getBalanceAfter())
                .description(t.getDescription())
                .apartmentTitle(t.getApartment() != null ? t.getApartment().getTitle() : null)
                .createdAt(t.getCreatedAt())
                .build();
    }
}
