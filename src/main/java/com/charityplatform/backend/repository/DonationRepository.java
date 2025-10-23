package com.charityplatform.backend.repository;

import com.charityplatform.backend.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {

    List<Donation> findByCampaignId(Long campaignId);

    List<Donation> findByUserId(Long userId);

    boolean existsByTransactionHash(String transactionHash);
}