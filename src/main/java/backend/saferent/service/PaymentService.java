package backend.saferent.service;

import backend.saferent.dto.request.payment.CreatePaymentRequest;
import backend.saferent.dto.response.payment.EscrowStatusResponse;
import backend.saferent.dto.response.payment.PaymentResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    PaymentResponse payDeposit(CreatePaymentRequest request);

    PaymentResponse payRent(CreatePaymentRequest request);

    List<PaymentResponse> getByContract(UUID contractId);

    EscrowStatusResponse getEscrowStatus(UUID contractId);

    PaymentResponse releaseDepositToTenant(UUID contractId);

    PaymentResponse releaseDepositToLandlord(UUID contractId);
}