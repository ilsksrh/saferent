package backend.saferent.service.impl;

import backend.saferent.dto.request.payment.CreatePaymentRequest;
import backend.saferent.dto.response.payment.EscrowStatusResponse;
import backend.saferent.dto.response.payment.PaymentResponse;
import backend.saferent.dto.response.payment.RentPeriodResponse;
import backend.saferent.entity.Contract;
import backend.saferent.entity.Payment;
import backend.saferent.entity.enums.ContractStatus;
import backend.saferent.entity.enums.NotificationType;
import backend.saferent.entity.enums.PaymentStatus;
import backend.saferent.entity.enums.PaymentType;
import backend.saferent.exception.BadRequestException;
import backend.saferent.exception.NotFoundException;
import backend.saferent.mapper.PaymentMapper;
import backend.saferent.repository.ContractRepository;
import backend.saferent.repository.PaymentRepository;
import backend.saferent.service.NotificationService;
import backend.saferent.service.PaymentService;
import backend.saferent.service.RentScheduleService;
import backend.saferent.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository   paymentRepository;
    private final ContractRepository  contractRepository;
    private final PaymentMapper       paymentMapper;
    private final NotificationService notificationService;
    private final RentScheduleService rentScheduleService;
    private final SecurityUtils       securityUtils;


    @Override
    @Transactional
    public PaymentResponse payDeposit(CreatePaymentRequest request) {
        Contract contract = getActiveContractOrThrow(request.getContractId());

        UUID payerId = securityUtils.getCurrentUserId();

        if (!contract.getTenant().getId().equals(payerId)) {
            throw new BadRequestException("Only the tenant can pay the deposit");
        }

        boolean alreadyPaid = paymentRepository.existsByContractAndTypeAndStatus(
                contract, PaymentType.DEPOSIT, PaymentStatus.PAID
        );
        if (alreadyPaid) {
            throw new BadRequestException(
                    "Deposit has already been paid for this contract"
            );
        }

        if (request.getAmount().compareTo(contract.getDepositAmount()) != 0) {
            throw new BadRequestException(
                    "Deposit amount must be exactly " +
                            contract.getDepositAmount() + " KZT"
            );
        }

        Payment payment = Payment.builder()
                .contract(contract)
                .type(PaymentType.DEPOSIT)
                .amount(request.getAmount())
                .method(request.getMethod())
                .status(PaymentStatus.PAID)
                .transactionId(generateTransactionId())
                .paidAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);

        notificationService.create(
                contract.getLandlord().getId(),
                "Deposit Received",
                contract.getTenant().getName() +
                        " paid the deposit of " + request.getAmount() +
                        " KZT for " + contract.getApartment().getTitle() +
                        ". It is safely held in escrow.",
                NotificationType.PAYMENT,
                saved.getId(),
                "PAYMENT"
        );

        notificationService.create(
                contract.getTenant().getId(),
                "Deposit Paid Successfully",
                "Your deposit of " + request.getAmount() +
                        " KZT is safely held in escrow. You can now move in!",
                NotificationType.PAYMENT,
                saved.getId(),
                "PAYMENT"
        );

        return paymentMapper.toResponse(saved);
    }


    @Override
    @Transactional
    public PaymentResponse payRent(CreatePaymentRequest request) {
        Contract contract = getActiveContractOrThrow(request.getContractId());

        UUID payerId = securityUtils.getCurrentUserId();

        if (!contract.getTenant().getId().equals(payerId)) {
            throw new BadRequestException("Only the tenant can pay rent");
        }

        boolean depositPaid = paymentRepository.existsByContractAndTypeAndStatus(
                contract, PaymentType.DEPOSIT, PaymentStatus.PAID
        );
        if (!depositPaid) {
            throw new BadRequestException(
                    "Deposit must be paid before making rent payments"
            );
        }

        if (request.getAmount().compareTo(contract.getRentAmount()) != 0) {
            throw new BadRequestException(
                    "Rent amount must be exactly " +
                            contract.getRentAmount() + " KZT"
            );
        }

        Payment payment = Payment.builder()
                .contract(contract)
                .type(PaymentType.RENT)
                .amount(request.getAmount())
                .method(request.getMethod())
                .status(PaymentStatus.PAID)
                .transactionId(generateTransactionId())
                .paidAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);

        rentScheduleService.markEarliestPaid(contract, saved);

        notificationService.create(
                contract.getLandlord().getId(),
                "Rent Payment Received",
                contract.getTenant().getName() +
                        " paid rent: " + request.getAmount() +
                        " KZT for " + contract.getApartment().getTitle(),
                NotificationType.PAYMENT,
                saved.getId(),
                "PAYMENT"
        );

        notificationService.create(
                contract.getTenant().getId(),
                "Rent Payment Confirmed",
                "Your rent payment of " + request.getAmount() +
                        " KZT was successful.",
                NotificationType.PAYMENT,
                saved.getId(),
                "PAYMENT"
        );

        return paymentMapper.toResponse(saved);
    }


    @Override
    public List<PaymentResponse> getByContract(UUID contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        return paymentRepository.findByContractOrderByCreatedAtDesc(contract)
                .stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }


    @Override
    public EscrowStatusResponse getEscrowStatus(UUID contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        Payment deposit = paymentRepository
                .findByContractAndType(contract, PaymentType.DEPOSIT)
                .orElse(null);

        String destination;
        String note;

        if (deposit == null) {
            destination = "NOT_PAID";
            note = "Deposit has not been paid yet";
        } else if (deposit.getStatus() == PaymentStatus.REFUNDED) {
            destination = "RETURNED";
            note = "Deposit has been returned";
        } else if (contract.getStatus() == ContractStatus.COMPLETED) {
            destination = "TENANT";
            note = "Contract completed. Deposit returned to tenant.";
        } else if (contract.getStatus() == ContractStatus.CANCELLED) {
            destination = "PENDING_REVIEW";
            note = "Contract cancelled. Deposit disposition under review.";
        } else {
            destination = "ESCROW";
            note = "Deposit is safely held in escrow. " +
                    "Will be returned to tenant after successful checkout.";
        }

        boolean depositPaid = deposit != null
                && deposit.getStatus() == PaymentStatus.PAID;

        return EscrowStatusResponse.builder()
                .contractId(contract.getId())
                .apartmentTitle(contract.getApartment().getTitle())
                .tenantName(contract.getTenant().getName())
                .landlordName(contract.getLandlord().getName())
                .depositAmount(contract.getDepositAmount())
                .depositStatus(deposit != null ? deposit.getStatus() : PaymentStatus.PENDING)
                .depositPaid(depositPaid)
                .depositPaidAt(deposit != null ? deposit.getPaidAt() : null)
                .depositReturnDestination(destination)
                .escrowNote(note)
                .build();
    }


    @Override
    @Transactional
    public PaymentResponse releaseDepositToTenant(UUID contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        UUID requestedBy = securityUtils.getCurrentUserId();

        boolean isLandlord = contract.getLandlord().getId().equals(requestedBy);
        boolean isTenant   = contract.getTenant().getId().equals(requestedBy);

        if (!isLandlord && !isTenant) {
            throw new BadRequestException("Not authorized to release deposit");
        }

        Payment deposit = paymentRepository
                .findByContractAndType(contract, PaymentType.DEPOSIT)
                .orElseThrow(() -> new NotFoundException(
                        "Deposit payment not found"
                ));

        if (deposit.getStatus() != PaymentStatus.PAID) {
            throw new BadRequestException("Deposit is not in PAID status");
        }

        deposit.setStatus(PaymentStatus.REFUNDED);
        deposit.setTransactionId("REFUND_TO_TENANT_" + generateTransactionId());

        contract.setStatus(ContractStatus.COMPLETED);
        contractRepository.save(contract);

        Payment saved = paymentRepository.save(deposit);

        notificationService.create(
                contract.getTenant().getId(),
                "Deposit Returned!",
                "Your deposit of " + contract.getDepositAmount() +
                        " KZT has been returned. Thank you for using SafeRent!",
                NotificationType.PAYMENT,
                saved.getId(),
                "PAYMENT"
        );

        return paymentMapper.toResponse(saved);
    }


    @Override
    @Transactional
    public PaymentResponse releaseDepositToLandlord(UUID contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        UUID requestedBy = securityUtils.getCurrentUserId();

        if (!contract.getLandlord().getId().equals(requestedBy)) {
            throw new BadRequestException(
                    "Only the landlord can claim the deposit"
            );
        }

        Payment deposit = paymentRepository
                .findByContractAndType(contract, PaymentType.DEPOSIT)
                .orElseThrow(() -> new NotFoundException(
                        "Deposit payment not found"
                ));

        if (deposit.getStatus() != PaymentStatus.PAID) {
            throw new BadRequestException("Deposit is not in PAID status");
        }

        deposit.setStatus(PaymentStatus.REFUNDED);
        deposit.setTransactionId(
                "TRANSFER_TO_LANDLORD_" + generateTransactionId()
        );

        contract.setStatus(ContractStatus.COMPLETED);
        contractRepository.save(contract);

        Payment saved = paymentRepository.save(deposit);

        notificationService.create(
                contract.getLandlord().getId(),
                "Deposit Transferred",
                "The deposit of " + contract.getDepositAmount() +
                        " KZT has been transferred to you due to damage claim.",
                NotificationType.PAYMENT,
                saved.getId(),
                "PAYMENT"
        );

        notificationService.create(
                contract.getTenant().getId(),
                "Deposit Transferred to Landlord",
                "Your deposit was transferred to the landlord " +
                        "due to confirmed damage.",
                NotificationType.PAYMENT,
                saved.getId(),
                "PAYMENT"
        );

        return paymentMapper.toResponse(saved);
    }


    @Override
    public List<RentPeriodResponse> getRentSchedule(UUID contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        return rentScheduleService.getSchedule(contract)
                .stream()
                .map(p -> RentPeriodResponse.builder()
                        .id(p.getId())
                        .periodIndex(p.getPeriodIndex())
                        .periodStart(p.getPeriodStart())
                        .periodEnd(p.getPeriodEnd())
                        .dueDate(p.getDueDate())
                        .amount(p.getAmount())
                        .status(p.getStatus())
                        .paidAt(p.getPaidAt())
                        .build())
                .collect(Collectors.toList());
    }

    private Contract getActiveContractOrThrow(UUID contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new BadRequestException(
                    "Contract must be ACTIVE to make payments. " +
                            "Current status: " + contract.getStatus()
            );
        }
        return contract;
    }

    private String generateTransactionId() {
        return "TXN_" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}