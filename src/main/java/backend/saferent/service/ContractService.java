package backend.saferent.service;

import backend.saferent.dto.request.contract.CreateContractRequest;
import backend.saferent.dto.response.contract.ContractResponse;

import java.util.List;
import java.util.UUID;

public interface ContractService {

    ContractResponse createContract(CreateContractRequest request);

    ContractResponse getById(UUID id);

    List<ContractResponse> getMyContracts();

    ContractResponse sign(UUID contractId);

    ContractResponse cancel(UUID contractId, String reason);
}