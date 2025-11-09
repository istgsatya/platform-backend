package com.charityplatform.backend.repository;

import com.charityplatform.backend.model.Donation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {

    List<Donation> findByCampaignId(Long campaignId);
    boolean existsByTransactionHash(String transactionHash);

    @EntityGraph(value = "Donation.withUserAndCampaign")
    List<Donation> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Donation d WHERE d.user.id = :userId")
    BigDecimal sumAmountByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(DISTINCT d.campaign.id) FROM Donation d WHERE d.user.id = :userId")
    long countDistinctCampaignsByUserId(@Param("userId") Long userId);


    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Donation d WHERE d.campaign.charity.id = :charityId")
    BigDecimal sumTotalDonationsByCharityId(@Param("charityId") Long charityId);
    @EntityGraph(value = "Donation.withUserAndCampaign")
    List<Donation> findByCampaignIdOrderByCreatedAtDesc(Long campaignId);
}