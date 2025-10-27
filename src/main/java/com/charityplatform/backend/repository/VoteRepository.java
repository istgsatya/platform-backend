package com.charityplatform.backend.repository;

import com.charityplatform.backend.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {
}