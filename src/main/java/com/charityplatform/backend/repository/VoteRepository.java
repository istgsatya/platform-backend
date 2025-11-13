package com.charityplatform.backend.repository;

import com.charityplatform.backend.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    @Query("SELECT COUNT(DISTINCT v.voter.id) FROM Vote v WHERE v.withdrawalRequest.id = :requestId")
    long countDistinctVotersByRequestId(@Param("requestId") Long requestId);
    long countByVoterId(Long userId);
}