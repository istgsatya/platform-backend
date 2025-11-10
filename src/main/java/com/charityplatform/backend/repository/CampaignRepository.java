package com.charityplatform.backend.repository;

import com.charityplatform.backend.model.Campaign;
import com.charityplatform.backend.model.CampaignStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    List<Campaign> findByStatus(CampaignStatus status);
    long countByCharityIdAndStatus(Long charityId, CampaignStatus status);


    @Query("SELECT c FROM Campaign c JOIN FETCH c.charity WHERE c.status = :status")
    List<Campaign> findByStatusWithCharity(@Param("status") CampaignStatus status);

    @Query("SELECT c FROM Campaign c JOIN FETCH c.charity WHERE c.id = :id")
    Optional<Campaign> findByIdWithCharity(@Param("id") Long id);


    @EntityGraph(attributePaths = {"charity"})
    List<Campaign> findByCharityIdOrderByCreatedAtDesc(Long charityId);

}