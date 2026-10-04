package com.cth.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cth_report_configs")
public class ReportConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reportName;

    private String description;

    @Column(nullable = false, length = 2000)
    private String querySql;

    private String targetTable;

    private LocalDateTime createdDate;

    public ReportConfig() {
        this.createdDate = LocalDateTime.now();
    }

    public ReportConfig(String reportName, String description, String querySql, String targetTable) {
        this();
        this.reportName = reportName;
        this.description = description;
        this.querySql = querySql;
        this.targetTable = targetTable;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getQuerySql() { return querySql; }
    public void setQuerySql(String querySql) { this.querySql = querySql; }

    public String getTargetTable() { return targetTable; }
    public void setTargetTable(String targetTable) { this.targetTable = targetTable; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}
