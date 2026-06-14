package backend.saferent.service;

import backend.saferent.entity.User;
import backend.saferent.entity.Wallet;
import backend.saferent.entity.WalletTransaction;
import backend.saferent.entity.enums.WalletTxnType;
import backend.saferent.exception.BadRequestException;
import backend.saferent.repository.WalletRepository;
import backend.saferent.repository.WalletTransactionRepository;
import backend.saferent.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock WalletRepository walletRepository;
    @Mock WalletTransactionRepository txnRepository;
    @Mock SecurityUtils securityUtils;

    @InjectMocks WalletService service;

    private User user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Tester");
        wallet = Wallet.builder().user(user).balance(new BigDecimal("1000")).build();

        lenient().when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        lenient().when(txnRepository.save(any(WalletTransaction.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void credit_increasesBalanceAndRecordsBalanceAfter() {
        WalletTransaction t = service.post(user, WalletTxnType.TOP_UP, new BigDecimal("500"), null, null, "topup");

        assertEquals(0, new BigDecimal("1500").compareTo(wallet.getBalance()));
        assertEquals(0, new BigDecimal("1500").compareTo(t.getBalanceAfter()));
        assertEquals(WalletTxnType.TOP_UP, t.getType());
    }

    @Test
    void debit_decreasesBalance() {
        service.post(user, WalletTxnType.RENT_PAYMENT, new BigDecimal("-300"), null, null, "rent");
        assertEquals(0, new BigDecimal("700").compareTo(wallet.getBalance()));
    }

    @Test
    void debit_insufficientFunds_throwsAndDoesNotSaveTxn() {
        assertThrows(BadRequestException.class,
                () -> service.post(user, WalletTxnType.WITHDRAWAL, new BigDecimal("-5000"), null, null, "withdraw"));
        verify(txnRepository, never()).save(any());
    }

    @Test
    void getOrCreateWallet_createsWhenAbsent() {
        User fresh = new User();
        fresh.setId(UUID.randomUUID());
        when(walletRepository.findByUser(fresh)).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));

        Wallet w = service.getOrCreateWallet(fresh);
        assertEquals(0, BigDecimal.ZERO.compareTo(w.getBalance()));
        verify(walletRepository).save(any(Wallet.class));
    }
}
