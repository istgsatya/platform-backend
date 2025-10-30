package com.charityplatform.backend.repository;

import com.charityplatform.backend.model.Donation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {

    List<Donation> findByCampaignId(Long campaignId);

    boolean existsByTransactionHash(String transactionHash);


    @EntityGraph(value = "Donation.withUserAndCampaign")
    List<Donation> findByUserIdOrderByCreatedAtDesc(Long userId);

}