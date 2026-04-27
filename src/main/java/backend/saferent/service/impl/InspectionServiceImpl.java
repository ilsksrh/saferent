package backend.saferent.service.impl;

import backend.saferent.client.AiAnalysisClient;
import backend.saferent.dto.response.inspection.InspectionCompareResponse;
import backend.saferent.dto.response.inspection.InspectionPhotoResponse;
import backend.saferent.entity.Contract;
import backend.saferent.entity.InspectionPhoto;
import backend.saferent.entity.enums.ContractStatus;
import backend.saferent.entity.enums.InspectionResult;
import backend.saferent.entity.enums.InspectionType;
import backend.saferent.entity.enums.NotificationType;
import backend.saferent.exception.BadRequestException;
import backend.saferent.exception.NotFoundException;
import backend.saferent.mapper.InspectionMapper;
import backend.saferent.repository.ContractRepository;
import backend.saferent.repository.InspectionPhotoRepository;
import backend.saferent.service.InspectionService;
import backend.saferent.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InspectionServiceImpl implements InspectionService {

    private final InspectionPhotoRepository inspectionPhotoRepository;
    private final ContractRepository        contractRepository;
    private final AiAnalysisClient          aiAnalysisClient;
    private final NotificationService       notificationService;
    private final InspectionMapper          inspectionMapper;

    private static final double SSIM_NO_DAMAGE    = 0.92;
    private static final double SSIM_MINOR_DAMAGE = 0.70;


    @Override
    @Transactional
    public InspectionPhotoResponse uploadCheckinPhoto(UUID contractId,
                                                      UUID uploadedBy,
                                                      String photoUrl,
                                                      String roomLabel) {

        Contract contract = getActiveContractOrThrow(contractId);

        if (!contract.getTenant().getId().equals(uploadedBy)) {
            throw new BadRequestException(
                    "Only the tenant can upload check-in photos"
            );
        }

        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new BadRequestException(
                    "Contract must be ACTIVE to upload check-in photos"
            );
        }

        InspectionPhoto photo = InspectionPhoto.builder()
                .contract(contract)
                .type(InspectionType.CHECKIN)
                .url(photoUrl)
                .roomLabel(roomLabel != null ? roomLabel : "general")
                .build();

        InspectionPhoto saved = inspectionPhotoRepository.save(photo);

        notificationService.create(
                contract.getLandlord().getId(),
                "Check-in Photos Uploaded",
                contract.getTenant().getName() +
                        " uploaded check-in photos for " +
                        contract.getApartment().getTitle(),
                NotificationType.CONTRACT,
                contract.getId(),
                "CONTRACT"
        );

        return inspectionMapper.toResponse(saved);
    }


    @Override
    @Transactional
    public InspectionPhotoResponse uploadCheckoutPhoto(UUID contractId,
                                                       UUID uploadedBy,
                                                       String photoUrl,
                                                       String roomLabel) {

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        if (!contract.getTenant().getId().equals(uploadedBy)) {
            throw new BadRequestException(
                    "Only the tenant can upload check-out photos"
            );
        }

        boolean hasCheckin = inspectionPhotoRepository
                .existsByContractAndType(contract, InspectionType.CHECKIN);
        if (!hasCheckin) {
            throw new BadRequestException(
                    "Check-in photos must be uploaded before check-out photos"
            );
        }

        InspectionPhoto photo = InspectionPhoto.builder()
                .contract(contract)
                .type(InspectionType.CHECKOUT)
                .url(photoUrl)
                .roomLabel(roomLabel != null ? roomLabel : "general")
                .build();

        InspectionPhoto saved = inspectionPhotoRepository.save(photo);

        notificationService.create(
                contract.getLandlord().getId(),
                "Check-out Photos Uploaded",
                contract.getTenant().getName() +
                        " uploaded check-out photos. " +
                        "AI analysis can now be started.",
                NotificationType.CONTRACT,
                contract.getId(),
                "CONTRACT"
        );

        return inspectionMapper.toResponse(saved);
    }


    @Override
    @Transactional
    public InspectionCompareResponse compare(UUID contractId, UUID requestedBy) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        boolean isParty = contract.getLandlord().getId().equals(requestedBy)
                || contract.getTenant().getId().equals(requestedBy);
        if (!isParty) {
            throw new BadRequestException("Not authorized");
        }

        List<InspectionPhoto> checkins = inspectionPhotoRepository
                .findByContractAndType(contract, InspectionType.CHECKIN);
        List<InspectionPhoto> checkouts = inspectionPhotoRepository
                .findByContractAndType(contract, InspectionType.CHECKOUT);

        if (checkins.isEmpty()) {
            throw new BadRequestException("No check-in photos found");
        }
        if (checkouts.isEmpty()) {
            throw new BadRequestException("No check-out photos found");
        }

        Map<String, Double> roomScores  = new LinkedHashMap<>();
        List<String> damagedRooms       = new ArrayList<>();

        Map<String, List<InspectionPhoto>> checkinByRoom = checkins.stream()
                .collect(Collectors.groupingBy(InspectionPhoto::getRoomLabel));
        Map<String, List<InspectionPhoto>> checkoutByRoom = checkouts.stream()
                .collect(Collectors.groupingBy(InspectionPhoto::getRoomLabel));

        Set<String> rooms = new LinkedHashSet<>(checkinByRoom.keySet());
        rooms.retainAll(checkoutByRoom.keySet());

        if (rooms.isEmpty()) {
            rooms.add("general");
            checkinByRoom.put("general", List.of(checkins.get(0)));
            checkoutByRoom.put("general", List.of(checkouts.get(0)));
        }

        for (String room : rooms) {
            String beforeUrl = checkinByRoom.get(room).get(0).getUrl();
            String afterUrl  = checkoutByRoom.get(room).get(0).getUrl();

            AiAnalysisClient.AiCompareResult aiResult =
                    aiAnalysisClient.comparePhotos(beforeUrl, afterUrl);

            double score = aiResult.getSsimScore() != null
                    ? aiResult.getSsimScore() : 0.95;

            roomScores.put(room, score);

            InspectionPhoto checkoutPhoto = checkoutByRoom.get(room).get(0);
            checkoutPhoto.setSsimScore(score);
            checkoutPhoto.setDamageDescription(aiResult.getDamageDescription());
            inspectionPhotoRepository.save(checkoutPhoto);

            if (score < SSIM_NO_DAMAGE) {
                damagedRooms.add(room);
            }
        }

        double avgScore = roomScores.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(1.0);

        InspectionResult result;
        String decision;
        String summary;

        if (avgScore >= SSIM_NO_DAMAGE) {
            result   = InspectionResult.NO_DAMAGE;
            decision = "RETURN_TO_TENANT";
            summary  = "No significant damage detected. " +
                    "Deposit will be fully returned to tenant. " +
                    "SSIM score: " + String.format("%.3f", avgScore);

            notifyDepositReturn(contract, true);

        } else if (avgScore >= SSIM_MINOR_DAMAGE) {
            result   = InspectionResult.MINOR_DAMAGE;
            decision = "MANUAL_REVIEW";
            summary  = "Minor changes detected in " + damagedRooms +
                    ". Human moderator will review and decide. " +
                    "SSIM score: " + String.format("%.3f", avgScore);

            notifyManualReview(contract, avgScore, damagedRooms);

        } else {
            result   = InspectionResult.MAJOR_DAMAGE;
            decision = "TRANSFER_TO_LANDLORD";
            summary  = "Significant damage detected in " + damagedRooms +
                    ". Deposit will be transferred to landlord. " +
                    "SSIM score: " + String.format("%.3f", avgScore);

            notifyDepositReturn(contract, false);
        }

        return InspectionCompareResponse.builder()
                .contractId(contract.getId())
                .averageSsimScore(avgScore)
                .roomScores(roomScores)
                .result(result)
                .depositDecision(decision)
                .damagedRooms(damagedRooms)
                .summary(summary)
                .build();
    }


    @Override
    public List<InspectionPhotoResponse> getAllByContract(UUID contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        return inspectionPhotoRepository
                .findByContractOrderByCreatedAtAsc(contract)
                .stream()
                .map(inspectionMapper::toResponse)
                .collect(Collectors.toList());
    }


    private Contract getActiveContractOrThrow(UUID contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new BadRequestException(
                    "Contract must be ACTIVE. Current: " + contract.getStatus()
            );
        }
        return contract;
    }

    private void notifyDepositReturn(Contract contract, boolean toTenant) {
        if (toTenant) {
            notificationService.create(
                    contract.getTenant().getId(),
                    "Deposit Will Be Returned",
                    "AI analysis complete: no significant damage detected. " +
                            "Your deposit of " + contract.getDepositAmount() +
                            " KZT will be returned.",
                    NotificationType.PAYMENT,
                    contract.getId(),
                    "CONTRACT"
            );
            notificationService.create(
                    contract.getLandlord().getId(),
                    "AI Analysis Complete — No Damage",
                    "Apartment condition is good. " +
                            "Deposit will be returned to tenant.",
                    NotificationType.PAYMENT,
                    contract.getId(),
                    "CONTRACT"
            );
        } else {
            notificationService.create(
                    contract.getLandlord().getId(),
                    "Damage Detected",
                    "AI analysis detected significant damage. " +
                            "Deposit of " + contract.getDepositAmount() +
                            " KZT will be transferred to you.",
                    NotificationType.PAYMENT,
                    contract.getId(),
                    "CONTRACT"
            );
            notificationService.create(
                    contract.getTenant().getId(),
                    "Deposit Claim by Landlord",
                    "AI analysis detected significant damage. " +
                            "Deposit will be transferred to the landlord.",
                    NotificationType.PAYMENT,
                    contract.getId(),
                    "CONTRACT"
            );
        }
    }

    private void notifyManualReview(Contract contract,
                                    double score,
                                    List<String> rooms) {
        String roomList = String.join(", ", rooms);

        notificationService.create(
                contract.getTenant().getId(),
                "Manual Review Required",
                "Minor changes detected in: " + roomList +
                        ". A moderator will review and decide on the deposit. " +
                        "SSIM: " + String.format("%.3f", score),
                NotificationType.SYSTEM,
                contract.getId(),
                "CONTRACT"
        );
        notificationService.create(
                contract.getLandlord().getId(),
                "Manual Review Required",
                "Minor changes detected in: " + roomList +
                        ". A moderator will review and decide on the deposit.",
                NotificationType.SYSTEM,
                contract.getId(),
                "CONTRACT"
        );
    }
}