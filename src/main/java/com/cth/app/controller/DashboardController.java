package com.cth.app.controller;

import com.cth.app.service.AppConfigService;
import com.cth.app.service.DatabaseExplorerService;
import com.cth.app.service.ReportEngineService;
import com.cth.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;

@Controller
@Tag(name = "Dashboard & Settings Controller", description = "Real-time system dashboard, connection metrics, dynamic configuration settings")
public class DashboardController {

    @Autowired
    private AppConfigService appConfigService;

    @Autowired
    private UserService userService;

    @Autowired
    private DatabaseExplorerService databaseExplorerService;

    @Autowired
    private ReportEngineService reportEngineService;

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("appName", appConfigService.getAppName());
        model.addAttribute("userCount", userService.getAllUsers().size());
        model.addAttribute("reportCount", reportEngineService.getAllReportConfigs().size());
        model.addAttribute("tableCount", databaseExplorerService.getAllTables().size());
        model.addAttribute("dbStatus", databaseExplorerService.getDatabaseConnectionStatus());
        model.addAttribute("authMode", appConfigService.getProperty("app.auth.mode", "LOCAL"));
        model.addAttribute("mfaEnabled", appConfigService.isMfaEnabled());
        return "dashboard";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("appName", appConfigService.getAppName());
        model.addAttribute("configs", appConfigService.getAllProperties());
        return "settings";
    }

    @PostMapping("/settings/save")
    public String saveSettings(@RequestParam Map<String, String> allParams, RedirectAttributes redirectAttributes) {
        try {
            appConfigService.updateConfiguration(allParams);
            redirectAttributes.addFlashAttribute("successMessage", "Configuration updated successfully! App name set to: " + appConfigService.getAppName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update settings: " + e.getMessage());
        }
        return "redirect:/settings";
    }

    @ResponseBody
    @GetMapping("/api/dashboard/realtime-metrics")
    @Operation(summary = "Realtime health, JVM memory, and DB connection metrics for dynamic dashboard")
    public Map<String, Object> getRealtimeMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        long heapMax = memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024);

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        metrics.put("appName", appConfigService.getAppName());
        metrics.put("heapUsedMb", heapUsed);
        metrics.put("heapMaxMb", heapMax);
        metrics.put("systemLoadAverage", osBean.getSystemLoadAverage());
        metrics.put("userCount", userService.getAllUsers().size());
        metrics.put("dbStatus", databaseExplorerService.getDatabaseConnectionStatus());
        metrics.put("timestamp", System.currentTimeMillis());

        return metrics;
    }
}
