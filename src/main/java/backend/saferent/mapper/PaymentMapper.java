package backend.saferent.mapper;

import backend.saferent.dto.response.payment.PaymentResponse;
import backend.saferent.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .contractId(p.getContract().getId())
                .apartmentTitle(p.getContract().getApartment().getTitle())
                .payerId(p.getContract().getTenant().getId())
                .payerName(p.getContract().getTenant().getName())
                .type(p.getType())
                .amount(p.getAmount())
                .method(p.getMethod())
                .status(p.getStatus())
                .transactionId(p.getTransactionId())
                .paidAt(p.getPaidAt())
                .createdAt(p.getCreatedAt())
                .build();
    }
}