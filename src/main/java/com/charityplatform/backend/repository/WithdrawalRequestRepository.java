package com.charityplatform.backend.repository;












import com.charityplatform.backend.model.RequestStatus;
import com.charityplatform.backend.model.WithdrawalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Long>  {

    List<WithdrawalRequest> findByStatus(RequestStatus status);
}
