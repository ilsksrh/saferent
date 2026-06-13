package backend.saferent.service.impl;

import backend.saferent.dto.request.contract.CreateContractRequest;
import backend.saferent.dto.response.contract.ContractResponse;
import backend.saferent.entity.Apartment;
import backend.saferent.entity.Contract;
import backend.saferent.entity.User;
import backend.saferent.entity.enums.ApartmentStatus;
import backend.saferent.entity.enums.ContractStatus;
import backend.saferent.entity.enums.NotificationType;
import backend.saferent.exception.BadRequestException;
import backend.saferent.exception.NotFoundException;
import backend.saferent.mapper.ContractMapper;
import backend.saferent.repository.ApartmentRepository;
import backend.saferent.repository.ContractRepository;
import backend.saferent.repository.UserRepository;
import backend.saferent.service.ContractService;
import backend.saferent.service.NotificationService;
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
public class ContractServiceImpl implements ContractService {

    private final ContractRepository   contractRepository;
    private final UserRepository       userRepository;
    private final ApartmentRepository  apartmentRepository;
    private final ContractMapper       contractMapper;
    private final NotificationService  notificationService;
    private final RentScheduleService  rentScheduleService;
    private final SecurityUtils        securityUtils;

    @Override
    @Transactional
    public ContractResponse createContract(CreateContractRequest request) {
        User tenant = userRepository.findById(request.getTenantId())
                .orElseThrow(() -> new NotFoundException("Tenant not found"));

        User landlord = userRepository.findById(request.getLandlordId())
                .orElseThrow(() -> new NotFoundException("Landlord not found"));

        Apartment apartment = apartmentRepository.findById(request.getApartmentId())
                .orElseThrow(() -> new NotFoundException("Apartment not found"));

        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        Contract contract = Contract.builder()
                .tenant(tenant)
                .landlord(landlord)
                .apartment(apartment)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .rentAmount(request.getRentAmount())
                .depositAmount(request.getDepositAmount())
                .status(ContractStatus.DRAFT)
                .terms(request.getTerms() != null
                        ? request.getTerms()
                        : "Standard SafeRent rental agreement.")
                .build();

        return contractMapper.toResponse(contractRepository.save(contract));
    }

    @Override
    public ContractResponse getById(UUID id) {
        return contractMapper.toResponse(getOrThrow(id));
    }

    @Override
    public List<ContractResponse> getMyContracts() {
        User user = securityUtils.getCurrentUser();

        return contractRepository.findByTenantOrLandlord(user)
                .stream()
                .map(contractMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ContractResponse sign(UUID contractId) {
        Contract contract = getOrThrow(contractId);

        UUID userId = securityUtils.getCurrentUserId();

        if (contract.getStatus() == ContractStatus.CANCELLED ||
                contract.getStatus() == ContractStatus.COMPLETED) {
            throw new BadRequestException(
                    "Cannot sign contract with status: " + contract.getStatus()
            );
        }

        boolean isTenant   = contract.getTenant().getId().equals(userId);
        boolean isLandlord = contract.getLandlord().getId().equals(userId);

        if (!isTenant && !isLandlord) {
            throw new BadRequestException(
                    "You are not a party to this contract"
            );
        }

        String signature = "SIGNED_BY_" + userId + "_AT_" + LocalDateTime.now();

        if (isTenant) {
            if (contract.getESignatureTenant() != null) {
                throw new BadRequestException("Tenant has already signed");
            }
            contract.setESignatureTenant(signature);
        }

        if (isLandlord) {
            if (contract.getESignatureLandlord() != null) {
                throw new BadRequestException("Landlord has already signed");
            }
            contract.setESignatureLandlord(signature);
        }

        if (contract.getESignatureTenant() != null &&
                contract.getESignatureLandlord() != null) {

            contract.setStatus(ContractStatus.ACTIVE);
            contract.setSignedAt(LocalDateTime.now());

            Apartment apt = contract.getApartment();
            apt.setStatus(ApartmentStatus.RENTED);
            apartmentRepository.save(apt);

            rentScheduleService.generateIfAbsent(contract);
            contract.setCheckinDeadline(contract.getStartDate().plusDays(2));
            contract.setCheckoutDeadline(contract.getEndDate().plusDays(2));

            notificationService.create(
                    contract.getTenant().getId(),
                    "Contract is Active!",
                    "Both parties signed the contract for " +
                            contract.getApartment().getTitle() +
                            ". Please pay the deposit.",
                    NotificationType.CONTRACT,
                    contract.getId(),
                    "CONTRACT"
            );
            notificationService.create(
                    contract.getLandlord().getId(),
                    "Contract is Active!",
                    "Both parties signed the contract for " +
                            contract.getApartment().getTitle() + ".",
                    NotificationType.CONTRACT,
                    contract.getId(),
                    "CONTRACT"
            );

        } else {
            contract.setStatus(ContractStatus.PENDING);

            UUID waitingFor = isTenant
                    ? contract.getLandlord().getId()
                    : contract.getTenant().getId();

            String signerName = isTenant
                    ? contract.getTenant().getName()
                    : contract.getLandlord().getName();

            notificationService.create(
                    waitingFor,
                    "Contract Awaiting Your Signature",
                    signerName + " signed the contract for " +
                            contract.getApartment().getTitle() +
                            ". Please sign to activate.",
                    NotificationType.CONTRACT,
                    contract.getId(),
                    "CONTRACT"
            );
        }

        return contractMapper.toResponse(contractRepository.save(contract));
    }

    @Override
    @Transactional
    public ContractResponse cancel(UUID contractId, String reason) {
        Contract contract = getOrThrow(contractId);

        UUID userId = securityUtils.getCurrentUserId();

        boolean isTenant   = contract.getTenant().getId().equals(userId);
        boolean isLandlord = contract.getLandlord().getId().equals(userId);

        if (!isTenant && !isLandlord) {
            throw new BadRequestException(
                    "You are not a party to this contract"
            );
        }

        if (contract.getStatus() == ContractStatus.COMPLETED ||
                contract.getStatus() == ContractStatus.CANCELLED) {
            throw new BadRequestException(
                    "Cannot cancel contract with status: " + contract.getStatus()
            );
        }

        if (contract.getStatus() == ContractStatus.ACTIVE) {
            Apartment apt = contract.getApartment();
            apt.setStatus(ApartmentStatus.ACTIVE);
            apartmentRepository.save(apt);
        }

        contract.setStatus(ContractStatus.CANCELLED);
        contract.setStatusReason(reason);

        return contractMapper.toResponse(contractRepository.save(contract));
    }

    private Contract getOrThrow(UUID id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Contract not found: " + id
                ));
    }
}