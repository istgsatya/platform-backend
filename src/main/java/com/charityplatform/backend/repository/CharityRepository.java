package com.charityplatform.backend.repository;

import com.charityplatform.backend.model.Charity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.charityplatform.backend.model.Charity;
import com.charityplatform.backend.model.VerificationStatus; // <-- ADD THIS IMPORT
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface CharityRepository extends JpaRepository<Charity,Long> {

    List<Charity> findByStatus(VerificationStatus status);
}
