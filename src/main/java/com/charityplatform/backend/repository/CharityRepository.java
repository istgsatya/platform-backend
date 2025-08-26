package com.charityplatform.backend.repository;

import com.charityplatform.backend.model.Charity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CharityRepository extends JpaRepository<Charity,Long> {
}
