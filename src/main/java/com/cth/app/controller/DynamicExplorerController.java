package com.cth.app.controller;

import com.cth.app.model.ReportConfig;
import com.cth.app.model.TableMetadata;
import com.cth.app.service.AppConfigService;
import com.cth.app.service.DatabaseExplorerService;
import com.cth.app.service.ReportEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@Tag(name = "Database Explorer & Online Reports", description = "Dynamic database schema viewer, field explorer, and configurable report builder")
public class DynamicExplorerController {

    @Autowired
    private DatabaseExplorerService databaseExplorerService;

    @Autowired
    private ReportEngineService reportEngineService;

    @Autowired
    private AppConfigService appConfigService;

    @GetMapping("/db-explorer")
    public String exploreDatabase(@RequestParam(required = false) String selectedTable, Model model) {
        model.addAttribute("appName", appConfigService.getAppName());
        List<String> tables = databaseExplorerService.getAllTables();
        model.addAttribute("tables", tables);

        if (selectedTable != null && !selectedTable.isEmpty()) {
            TableMetadata metadata = databaseExplorerService.getTableMetadata(selectedTable);
            model.addAttribute("selectedTable", selectedTable);
            model.addAttribute("metadata", metadata);

            try {
                // Fetch sample data preview (first 10 rows)
                String previewSql = "SELECT * FROM " + selectedTable + " FETCH FIRST 10 ROWS ONLY";
                List<Map<String, Object>> previewData;
                try {
                    previewData = databaseExplorerService.executeQuery(previewSql);
                } catch (Exception e) {
                    // Fallback for databases like H2/PostgreSQL/MySQL
                    previewData = databaseExplorerService.executeQuery("SELECT * FROM " + selectedTable + " LIMIT 10");
                }
                model.addAttribute("previewData", previewData);
            } catch (Exception e) {
                model.addAttribute("previewError", "Could not preview data: " + e.getMessage());
            }
        }

        return "db-explorer";
    }

    @GetMapping("/reports")
    public String viewReports(@RequestParam(required = false) Long reportId, Model model) {
        model.addAttribute("appName", appConfigService.getAppName());
        List<ReportConfig> reports = reportEngineService.getAllReportConfigs();
        model.addAttribute("reports", reports);

        if (reportId != null) {
            reportEngineService.getReportById(reportId).ifPresent(report -> {
                model.addAttribute("selectedReport", report);
                try {
                    List<Map<String, Object>> reportData = reportEngineService.executeReport(reportId);
                    model.addAttribute("reportData", reportData);
                } catch (Exception e) {
                    model.addAttribute("reportError", "Error running report query: " + e.getMessage());
                }
            });
        }

        return "reports";
    }

    @PostMapping("/reports/save")
    public String saveReportConfig(@ModelAttribute ReportConfig reportConfig, RedirectAttributes redirectAttributes) {
        try {
            reportEngineService.saveReportConfig(reportConfig);
            redirectAttributes.addFlashAttribute("successMessage", "Report configuration saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to save report: " + e.getMessage());
        }
        return "redirect:/reports";
    }

    @GetMapping("/reports/{id}/export-csv")
    public void exportReportCsv(@PathVariable Long id, HttpServletResponse response) throws IOException {
        ReportConfig report = reportEngineService.getReportById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid report id: " + id));

        List<Map<String, Object>> data = reportEngineService.executeReport(id);
        String csvContent = reportEngineService.generateCsv(data);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + report.getReportName().replaceAll("\\s+", "_") + ".csv\"");
        response.getWriter().write(csvContent);
    }

    @ResponseBody
    @GetMapping("/api/tables")
    @Operation(summary = "Get database tables list")
    public List<String> getTablesApi() {
        return databaseExplorerService.getAllTables();
    }
}
