package com.charityplatform.backend.repository;


import com.charityplatform.backend.model.Campaign;
import com.charityplatform.backend.model.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query; // <-- Add this import
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;


@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    List<Campaign> findByCharityId(Long charityId);
    List<Campaign> findByStatus(CampaignStatus status);


    @Query("SELECT c FROM Campaign c JOIN FETCH c.charity WHERE c.status = :status")
    List<Campaign> findByStatusWithCharity(@Param("status") CampaignStatus status);

    @Query("SELECT c FROM Campaign c JOIN FETCH c.charity WHERE c.id = :id")
    Optional<Campaign> findByIdWithCharity(@Param("id") Long id);

}
