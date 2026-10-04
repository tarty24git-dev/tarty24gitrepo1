package com.cth.app.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AppConfigService {

    private static final Logger logger = LoggerFactory.getLogger(AppConfigService.class);
    private static final String CONFIG_FILE_NAME = "application-custom.properties";

    private final Properties properties = new Properties();
    private final Map<String, String> runtimeCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        loadProperties();
    }

    public synchronized void loadProperties() {
        File customFile = new File(CONFIG_FILE_NAME);
        if (customFile.exists()) {
            try (FileInputStream fis = new FileInputStream(customFile)) {
                properties.load(fis);
                logger.info("Loaded custom properties from {}", customFile.getAbsolutePath());
            } catch (IOException e) {
                logger.error("Failed to load {}", CONFIG_FILE_NAME, e);
            }
        } else {
            // Load defaults
            setDefaultProperties();
            saveProperties();
        }

        // Sync properties to runtimeCache
        properties.forEach((k, v) -> runtimeCache.put(String.valueOf(k), String.valueOf(v)));
    }

    private void setDefaultProperties() {
        properties.setProperty("app.name", "CTH Enterprise Dynamic System");
        properties.setProperty("db.type", "H2"); // ORACLE, POSTGRESQL, SQLSERVER, H2
        properties.setProperty("db.url", "jdbc:h2:mem:cthdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        properties.setProperty("db.driver", "org.h2.Driver");
        properties.setProperty("db.username", "sa");
        properties.setProperty("db.password", "");
        properties.setProperty("app.auth.mode", "LOCAL"); // LOCAL or LDAP
        properties.setProperty("app.auth.ldap.url", "ldap://localhost:389");
        properties.setProperty("app.auth.ldap.base-dn", "dc=cth,dc=com");
        properties.setProperty("app.auth.ldap.user-dn-pattern", "uid={0},ou=users");
        properties.setProperty("app.security.mfa.enabled", "false");
        properties.setProperty("app.security.password.expiration-days", "90");
        properties.setProperty("app.security.max-failed-attempts", "5");
    }

    public synchronized void saveProperties() {
        File customFile = new File(CONFIG_FILE_NAME);
        try (FileOutputStream fos = new FileOutputStream(customFile)) {
            properties.store(fos, "CTH Dynamic Application Configuration");
            logger.info("Saved dynamic properties to {}", customFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to save properties to {}", CONFIG_FILE_NAME, e);
        }
    }

    public String getProperty(String key, String defaultValue) {
        return runtimeCache.getOrDefault(key, properties.getProperty(key, defaultValue));
    }

    public synchronized void setProperty(String key, String value) {
        if (value == null) value = "";
        properties.setProperty(key, value);
        runtimeCache.put(key, value);
    }

    public synchronized void updateConfiguration(Map<String, String> newSettings) {
        newSettings.forEach((k, v) -> {
            if (v != null) {
                properties.setProperty(k, v);
                runtimeCache.put(k, v);
            }
        });
        saveProperties();
    }

    public Map<String, String> getAllProperties() {
        return new ConcurrentHashMap<>(runtimeCache);
    }

    public String getAppName() {
        return getProperty("app.name", "CTH Enterprise App");
    }

    public boolean isLdapEnabled() {
        return "LDAP".equalsIgnoreCase(getProperty("app.auth.mode", "LOCAL"));
    }

    public boolean isMfaEnabled() {
        return "true".equalsIgnoreCase(getProperty("app.security.mfa.enabled", "false"));
    }

    public int getMaxFailedAttempts() {
        try {
            return Integer.parseInt(getProperty("app.security.max-failed-attempts", "5"));
        } catch (NumberFormatException e) {
            return 5;
        }
    }

    public int getPasswordExpirationDays() {
        try {
            return Integer.parseInt(getProperty("app.security.password.expiration-days", "90"));
        } catch (NumberFormatException e) {
            return 90;
        }
    }
}
