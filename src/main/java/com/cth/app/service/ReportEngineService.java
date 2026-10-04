package com.cth.app.service;

import com.cth.app.model.ReportConfig;
import com.cth.app.repository.ReportConfigRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReportEngineService {

    private static final Logger logger = LoggerFactory.getLogger(ReportEngineService.class);

    @Autowired
    private ReportConfigRepository reportConfigRepository;

    @Autowired
    private DatabaseExplorerService databaseExplorerService;

    @PostConstruct
    public void initDefaultReports() {
        if (reportConfigRepository.count() == 0) {
            ReportConfig userReport = new ReportConfig(
                    "User Activity & Status Report",
                    "List of all registered users and account status",
                    "SELECT username, full_name, email, role, account_non_locked, enabled, failed_attempt FROM cth_users",
                    "cth_users"
            );
            reportConfigRepository.save(userReport);
            logger.info("Initialized default online report configuration");
        }
    }

    public List<ReportConfig> getAllReportConfigs() {
        return reportConfigRepository.findAll();
    }

    public Optional<ReportConfig> getReportById(Long id) {
        return reportConfigRepository.findById(id);
    }

    public ReportConfig saveReportConfig(ReportConfig reportConfig) {
        return reportConfigRepository.save(reportConfig);
    }

    public void deleteReportConfig(Long id) {
        reportConfigRepository.deleteById(id);
    }

    public List<Map<String, Object>> executeReport(Long reportId) {
        ReportConfig config = reportConfigRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report configuration not found for ID: " + reportId));
        return databaseExplorerService.executeQuery(config.getQuerySql());
    }

    public String generateCsv(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            return "No data available";
        }

        StringBuilder sb = new StringBuilder();
        // Header
        Map<String, Object> firstRow = data.get(0);
        sb.append(String.join(",", firstRow.keySet())).append("\n");

        // Rows
        for (Map<String, Object> row : data) {
            List<String> values = new ArrayList<>();
            for (Object val : row.values()) {
                values.add(val != null ? "\"" + val.toString().replace("\"", "\"\"") + "\"" : "");
            }
            sb.append(String.join(",", values)).append("\n");
        }

        return sb.toString();
    }
}
