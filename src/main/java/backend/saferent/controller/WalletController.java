package backend.saferent.controller;

import backend.saferent.dto.request.wallet.TopUpRequest;
import backend.saferent.dto.request.wallet.WithdrawRequest;
import backend.saferent.dto.response.wallet.WalletResponse;
import backend.saferent.dto.response.wallet.WalletTransactionResponse;
import backend.saferent.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallet")
@Tag(name = "Wallet", description = "Кошелёк пользователя")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @Operation(summary = "Мой кошелёк (баланс + последние транзакции)")
    @GetMapping("/me")
    public ResponseEntity<WalletResponse> myWallet() {
        return ResponseEntity.ok(walletService.getMyWallet());
    }

    @Operation(summary = "История транзакций кошелька")
    @GetMapping("/me/transactions")
    public ResponseEntity<List<WalletTransactionResponse>> myTransactions() {
        return ResponseEntity.ok(walletService.getMyTransactions());
    }

    @Operation(summary = "Пополнить кошелёк (mock)")
    @PostMapping("/me/topup")
    public ResponseEntity<WalletResponse> topUp(@Valid @RequestBody TopUpRequest request) {
        return ResponseEntity.ok(walletService.topUp(request.getAmount(), request.getMethod()));
    }

    @Operation(summary = "Вывести средства (mock)")
    @PostMapping("/me/withdraw")
    public ResponseEntity<WalletResponse> withdraw(@Valid @RequestBody WithdrawRequest request) {
        return ResponseEntity.ok(walletService.withdraw(request.getAmount()));
    }
}
