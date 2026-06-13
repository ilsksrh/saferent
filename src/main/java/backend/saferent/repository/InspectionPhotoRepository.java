package backend.saferent.repository;

import backend.saferent.entity.Contract;
import backend.saferent.entity.InspectionPhoto;
import backend.saferent.entity.enums.InspectionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InspectionPhotoRepository extends JpaRepository<InspectionPhoto, UUID> {

    List<InspectionPhoto> findByContractAndType(
            Contract contract, InspectionType type
    );

    List<InspectionPhoto> findByContractOrderByCreatedAtAsc(Contract contract);

    boolean existsByContractAndType(Contract contract, InspectionType type);

    List<InspectionPhoto> findAllByContractId(UUID contractId);
}