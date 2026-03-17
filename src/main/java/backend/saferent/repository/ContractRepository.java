package backend.saferent.repository;

import backend.saferent.entity.Contract;
import backend.saferent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContractRepository extends JpaRepository<Contract, UUID> {

    List<Contract> findByTenant(User tenant);

    List<Contract> findByLandlord(User landlord);

}