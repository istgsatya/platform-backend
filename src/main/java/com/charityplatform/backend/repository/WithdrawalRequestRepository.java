package com.charityplatform.backend.repository;






import com.charityplatform.backend.model.RequestStatus;
import com.charityplatform.backend.model.WithdrawalRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Long>  {

    List<WithdrawalRequest> findByStatus(RequestStatus status);


    @EntityGraph(value = "WithdrawalRequest.withCampaign")
    List<WithdrawalRequest> findByCampaignIdOrderByCreatedAtDesc(Long campaignId);
    @Query("SELECT COUNT(wr) FROM WithdrawalRequest wr WHERE wr.campaign.charity.id = :charityId AND wr.status = 'PENDING_VOTE'")
    long countPendingByCharityId(@Param("charityId") Long charityId);
}