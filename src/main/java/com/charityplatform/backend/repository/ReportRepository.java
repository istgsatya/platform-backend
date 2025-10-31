package com.charityplatform.backend.repository;

import com.charityplatform.backend.model.Report;
import com.charityplatform.backend.model.ReportStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    /**
     * Finds all reports with a given status.
     * Uses a comprehensive EntityGraph to eagerly fetch all nested associations
     * (reporter, request, campaign, charity) to prevent LazyInitializationExceptions.
     *
     * @param status The status to filter by (e.g., PENDING).
     * @return A list of fully initialized Report objects.
     */
    @EntityGraph(value = "Report.withAllDetails")
    List<Report> findByStatus(ReportStatus status);
}