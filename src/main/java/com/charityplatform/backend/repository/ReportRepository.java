package com.charityplatform.backend.repository;

import com.charityplatform.backend.model.Report;
import com.charityplatform.backend.model.ReportStatus; // <-- New Import
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List; // <-- New Import

public interface ReportRepository extends JpaRepository<Report, Long> {


    List<Report> findByStatus(ReportStatus status);
}