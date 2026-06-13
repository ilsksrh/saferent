package backend.saferent.service.impl;

import backend.saferent.dto.request.payment.CreatePaymentRequest;
import backend.saferent.dto.response.payment.EscrowStatusResponse;
import backend.saferent.dto.response.payment.PaymentResponse;
import backend.saferent.entity.Apartment;
import backend.saferent.entity.Contract;
import backend.saferent.entity.Payment;
import backend.saferent.entity.User;
import backend.saferent.entity.enums.ContractStatus;
import backend.saferent.entity.enums.NotificationType;
import backend.saferent.entity.enums.PaymentMethod;
import backend.saferent.entity.enums.PaymentStatus;
import backend.saferent.entity.enums.PaymentType;
import backend.saferent.exception.BadRequestException;
import backend.saferent.mapper.PaymentMapper;
import backend.saferent.repository.ContractRepository;
import backend.saferent.repository.PaymentRepository;
import backend.saferent.service.NotificationService;
import backend.saferent.service.RentScheduleService;
import backend.saferent.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock PaymentRepository paymentRepository;
    @Mock ContractRepository contractRepository;
    @Mock PaymentMapper paymentMapper;
    @Mock NotificationService notificationService;
    @Mock RentScheduleService rentScheduleService;
    @Mock SecurityUtils securityUtils;

    @InjectMocks PaymentServiceImpl service;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID landlordId = UUID.randomUUID();
    private final UUID contractId = UUID.randomUUID();

    private User tenant;
    private User landlord;
    private Contract contract;

    @BeforeEach
    void setUp() {
        tenant = new User();
        tenant.setId(tenantId);
        tenant.setName("Tenant");

        landlord = new User();
        landlord.setId(landlordId);
        landlord.setName("Landlord");

        Apartment apartment = new Apartment();
        apartment.setTitle("Cozy flat");

        contract = new Contract();
        contract.setId(contractId);
        contract.setTenant(tenant);
        contract.setLandlord(landlord);
        contract.setApartment(apartment);
        contract.setStatus(ContractStatus.ACTIVE);
        contract.setDepositAmount(new BigDecimal("100000"));
        contract.setRentAmount(new BigDecimal("50000"));
    }

    private CreatePaymentRequest request(BigDecimal amount, PaymentType type) {
        CreatePaymentRequest r = new CreatePaymentRequest();
        r.setContractId(contractId);
        r.setType(type);
        r.setAmount(amount);
        r.setMethod(PaymentMethod.KASPI);
        return r;
    }

    // ─── payDeposit ──────────────────────────────────────────────────────

    @Test
    void payDeposit_success_savesPaidDepositAndNotifiesBothParties() {
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(securityUtils.getCurrentUserId()).thenReturn(tenantId);
        when(paymentRepository.existsByContractAndTypeAndStatus(
                contract, PaymentType.DEPOSIT, PaymentStatus.PAID)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentMapper.toResponse(any())).thenReturn(mock(PaymentResponse.class));

        service.payDeposit(request(new BigDecimal("100000"), PaymentType.DEPOSIT));

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        Payment saved = captor.getValue();
        assertEquals(PaymentStatus.PAID, saved.getStatus());
        assertEquals(PaymentType.DEPOSIT, saved.getType());
        assertNotNull(saved.getTransactionId());
        assertTrue(saved.getTransactionId().startsWith("TXN_"));
        assertNotNull(saved.getPaidAt());

        verify(notificationService).create(eq(landlordId), any(), any(),
                eq(NotificationType.PAYMENT), any(), any());
        verify(notificationService).create(eq(tenantId), any(), any(),
                eq(NotificationType.PAYMENT), any(), any());
    }

    @Test
    void payDeposit_byNonTenant_throws() {
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(securityUtils.getCurrentUserId()).thenReturn(landlordId);

        assertThrows(BadRequestException.class,
                () -> service.payDeposit(request(new BigDecimal("100000"), PaymentType.DEPOSIT)));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void payDeposit_whenAlreadyPaid_throws() {
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(securityUtils.getCurrentUserId()).thenReturn(tenantId);
        when(paymentRepository.existsByContractAndTypeAndStatus(
                contract, PaymentType.DEPOSIT, PaymentStatus.PAID)).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> service.payDeposit(request(new BigDecimal("100000"), PaymentType.DEPOSIT)));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void payDeposit_withWrongAmount_throws() {
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(securityUtils.getCurrentUserId()).thenReturn(tenantId);
        when(paymentRepository.existsByContractAndTypeAndStatus(
                contract, PaymentType.DEPOSIT, PaymentStatus.PAID)).thenReturn(false);

        assertThrows(BadRequestException.class,
                () -> service.payDeposit(request(new BigDecimal("99999"), PaymentType.DEPOSIT)));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void payDeposit_whenContractNotActive_throws() {
        contract.setStatus(ContractStatus.PENDING);
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));

        assertThrows(BadRequestException.class,
                () -> service.payDeposit(request(new BigDecimal("100000"), PaymentType.DEPOSIT)));
    }

    // ─── payRent ─────────────────────────────────────────────────────────

    @Test
    void payRent_success_marksEarliestPeriodPaid() {
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(securityUtils.getCurrentUserId()).thenReturn(tenantId);
        when(paymentRepository.existsByContractAndTypeAndStatus(
                contract, PaymentType.DEPOSIT, PaymentStatus.PAID)).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentMapper.toResponse(any())).thenReturn(mock(PaymentResponse.class));

        service.payRent(request(new BigDecimal("50000"), PaymentType.RENT));

        verify(rentScheduleService).markEarliestPaid(eq(contract), any(Payment.class));
    }

    @Test
    void payRent_beforeDepositPaid_throws() {
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(securityUtils.getCurrentUserId()).thenReturn(tenantId);
        when(paymentRepository.existsByContractAndTypeAndStatus(
                contract, PaymentType.DEPOSIT, PaymentStatus.PAID)).thenReturn(false);

        assertThrows(BadRequestException.class,
                () -> service.payRent(request(new BigDecimal("50000"), PaymentType.RENT)));
        verify(paymentRepository, never()).save(any());
    }

    // ─── getEscrowStatus ─────────────────────────────────────────────────

    @Test
    void escrowStatus_whenNoDeposit_isNotPaid() {
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(paymentRepository.findByContractAndType(contract, PaymentType.DEPOSIT))
                .thenReturn(Optional.empty());

        EscrowStatusResponse res = service.getEscrowStatus(contractId);
        assertEquals("NOT_PAID", res.getDepositReturnDestination());
        assertFalse(res.isDepositPaid());
    }

    @Test
    void escrowStatus_whenRefunded_isReturned() {
        Payment deposit = new Payment();
        deposit.setStatus(PaymentStatus.REFUNDED);
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(paymentRepository.findByContractAndType(contract, PaymentType.DEPOSIT))
                .thenReturn(Optional.of(deposit));

        EscrowStatusResponse res = service.getEscrowStatus(contractId);
        assertEquals("RETURNED", res.getDepositReturnDestination());
    }

    @Test
    void escrowStatus_whenPaidAndActive_isEscrow() {
        Payment deposit = new Payment();
        deposit.setStatus(PaymentStatus.PAID);
        deposit.setPaidAt(LocalDateTime.now());
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(paymentRepository.findByContractAndType(contract, PaymentType.DEPOSIT))
                .thenReturn(Optional.of(deposit));

        EscrowStatusResponse res = service.getEscrowStatus(contractId);
        assertEquals("ESCROW", res.getDepositReturnDestination());
        assertTrue(res.isDepositPaid());
    }

    // ─── release ─────────────────────────────────────────────────────────

    @Test
    void releaseToTenant_success_refundsDepositAndCompletesContract() {
        Payment deposit = new Payment();
        deposit.setStatus(PaymentStatus.PAID);
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(paymentRepository.findByContractAndType(contract, PaymentType.DEPOSIT))
                .thenReturn(Optional.of(deposit));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentMapper.toResponse(any())).thenReturn(mock(PaymentResponse.class));

        service.releaseDepositToTenant(contractId);

        assertEquals(PaymentStatus.REFUNDED, deposit.getStatus());
        assertTrue(deposit.getTransactionId().startsWith("REFUND_TO_TENANT_"));
        assertEquals(ContractStatus.COMPLETED, contract.getStatus());
        verify(notificationService).create(eq(tenantId), any(), any(),
                eq(NotificationType.PAYMENT), any(), any());
    }

    @Test
    void releaseToLandlord_success_refundsToLandlordAndCompletes() {
        Payment deposit = new Payment();
        deposit.setStatus(PaymentStatus.PAID);
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(paymentRepository.findByContractAndType(contract, PaymentType.DEPOSIT))
                .thenReturn(Optional.of(deposit));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentMapper.toResponse(any())).thenReturn(mock(PaymentResponse.class));

        service.releaseDepositToLandlord(contractId);

        assertEquals(PaymentStatus.REFUNDED, deposit.getStatus());
        assertTrue(deposit.getTransactionId().startsWith("TRANSFER_TO_LANDLORD_"));
        assertEquals(ContractStatus.COMPLETED, contract.getStatus());
        verify(notificationService).create(eq(landlordId), any(), any(),
                eq(NotificationType.PAYMENT), any(), any());
    }
}
