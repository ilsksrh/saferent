package backend.saferent.repository;

import backend.saferent.entity.Contract;
import backend.saferent.entity.User;
import backend.saferent.entity.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ContractRepository extends JpaRepository<Contract, UUID> {

    List<Contract> findByTenant(User tenant);

    List<Contract> findByLandlord(User landlord);

    @Query("SELECT c FROM Contract c WHERE c.tenant = :user OR c.landlord = :user")
    List<Contract> findByTenantOrLandlord(@Param("user") User user);

    List<Contract> findByStatusAndInspectionDecidedAtIsNotNull(ContractStatus status);

}