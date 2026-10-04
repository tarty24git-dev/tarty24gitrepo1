package com.cth.app.service;

import com.cth.app.model.ReportConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class DynamicDatabaseAndReportTest {

    @Autowired
    private DatabaseExplorerService databaseExplorerService;

    @Autowired
    private ReportEngineService reportEngineService;

    @Test
    public void testGetAllTables() {
        List<String> tables = databaseExplorerService.getAllTables();
        assertNotNull(tables);
        assertTrue(tables.contains("CTH_USERS") || tables.contains("cth_users") || tables.stream().anyMatch(t -> t.equalsIgnoreCase("cth_users")));
    }

    @Test
    public void testCreateAndExecuteReport() {
        ReportConfig report = new ReportConfig("JUnit Test Report", "Test description", "SELECT username, role FROM cth_users", "cth_users");
        ReportConfig saved = reportEngineService.saveReportConfig(report);

        assertNotNull(saved.getId());

        List<Map<String, Object>> results = reportEngineService.executeReport(saved.getId());
        assertNotNull(results);
        assertFalse(results.isEmpty());

        String csv = reportEngineService.generateCsv(results);
        assertNotNull(csv);
        assertTrue(csv.contains("username") || csv.contains("USERNAME"));
    }
}
