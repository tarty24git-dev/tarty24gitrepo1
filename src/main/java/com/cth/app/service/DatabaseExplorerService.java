package com.cth.app.service;

import com.cth.app.model.TableMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Service
public class DatabaseExplorerService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseExplorerService.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<String> getAllTables() {
        List<String> tables = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE", "VIEW"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    tables.add(tableName);
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving database tables", e);
        }
        return tables;
    }

    public TableMetadata getTableMetadata(String tableName) {
        List<TableMetadata.ColumnMetadata> columns = new ArrayList<>();
        String tableType = "TABLE";

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            // Get Table Type
            try (ResultSet rs = metaData.getTables(null, null, tableName, null)) {
                if (rs.next()) {
                    tableType = rs.getString("TABLE_TYPE");
                }
            }

            // Get Columns
            try (ResultSet rs = metaData.getColumns(null, null, tableName, "%")) {
                while (rs.next()) {
                    String colName = rs.getString("COLUMN_NAME");
                    String dataType = rs.getString("TYPE_NAME");
                    int colSize = rs.getInt("COLUMN_SIZE");
                    boolean nullable = "YES".equalsIgnoreCase(rs.getString("IS_NULLABLE"));
                    columns.add(new TableMetadata.ColumnMetadata(colName, dataType, colSize, nullable));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving table metadata for {}", tableName, e);
        }

        return new TableMetadata(tableName, tableType, columns);
    }

    public List<Map<String, Object>> executeQuery(String sql) {
        // Basic safety check for demo SELECT queries
        String trimmed = sql.trim().toUpperCase();
        if (!trimmed.startsWith("SELECT")) {
            throw new IllegalArgumentException("Only SELECT queries are allowed in Report Explorer");
        }
        return jdbcTemplate.queryForList(sql);
    }

    public Map<String, Object> getDatabaseConnectionStatus() {
        Map<String, Object> status = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            status.put("connected", true);
            status.put("dbProductName", metaData.getDatabaseProductName());
            status.put("dbProductVersion", metaData.getDatabaseProductVersion());
            status.put("driverName", metaData.getDriverName());
            status.put("url", metaData.getURL());
            status.put("username", metaData.getUserName());
        } catch (Exception e) {
            status.put("connected", false);
            status.put("error", e.getMessage());
        }
        return status;
    }
}
