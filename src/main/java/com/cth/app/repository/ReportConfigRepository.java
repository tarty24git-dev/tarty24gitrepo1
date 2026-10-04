package com.cth.app.repository;

import com.cth.app.model.ReportConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportConfigRepository extends JpaRepository<ReportConfig, Long> {
    Optional<ReportConfig> findByReportName(String reportName);
}
