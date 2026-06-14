package backend.saferent.mapper;

import backend.saferent.dto.response.contract.ContractResponse;
import backend.saferent.entity.Contract;
import org.springframework.stereotype.Component;

@Component
public class ContractMapper {

    public ContractResponse toResponse(Contract c) {
        return ContractResponse.builder()
                .id(c.getId())
                .tenantId(c.getTenant().getId())
                .tenantName(c.getTenant().getName())
                .tenantPhone(c.getTenant().getPhone())
                .landlordId(c.getLandlord().getId())
                .landlordName(c.getLandlord().getName())
                .landlordPhone(c.getLandlord().getPhone())
                .apartmentId(c.getApartment().getId())
                .apartmentTitle(c.getApartment().getTitle())
                .apartmentAddress(c.getApartment().getAddress())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .rentAmount(c.getRentAmount())
                .depositAmount(c.getDepositAmount())
                .status(c.getStatus())
                .statusReason(c.getStatusReason())
                .terms(c.getTerms())
                .tenantSigned(c.getESignatureTenant() != null)
                .landlordSigned(c.getESignatureLandlord() != null)
                .signedAt(c.getSignedAt())
                .checkinDeadline(c.getCheckinDeadline())
                .checkinConfirmedAt(c.getCheckinConfirmedAt())
                .checkoutDeadline(c.getCheckoutDeadline())
                .inspectionResult(c.getInspectionResult())
                .inspectionAvgSsim(c.getInspectionAvgSsim())
                .inspectionDecidedAt(c.getInspectionDecidedAt())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}