package com.cth.app.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AppConfigServiceTest {

    @Autowired
    private AppConfigService appConfigService;

    @Test
    public void testUpdateAppNameAndProperties() {
        Map<String, String> updates = new HashMap<>();
        updates.put("app.name", "JUnit Test Application");
        updates.put("app.auth.mode", "LDAP");

        appConfigService.updateConfiguration(updates);

        assertEquals("JUnit Test Application", appConfigService.getAppName());
        assertTrue(appConfigService.isLdapEnabled());

        // Restore default
        updates.put("app.name", "CTH Enterprise Dynamic System");
        updates.put("app.auth.mode", "LOCAL");
        appConfigService.updateConfiguration(updates);
    }
}
