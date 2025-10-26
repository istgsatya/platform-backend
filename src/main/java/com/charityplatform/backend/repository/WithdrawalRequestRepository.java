package com.charityplatform.backend.repository;












import com.charityplatform.backend.model.WithdrawalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;;;;

@Repository
public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Long>  {

}
