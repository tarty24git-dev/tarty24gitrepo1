-- SQL Server DDL and Initial Seed Script for CTH Enterprise Application

IF OBJECT_ID('cth_users', 'U') IS NOT NULL DROP TABLE cth_users;
IF OBJECT_ID('cth_report_configs', 'U') IS NOT NULL DROP TABLE cth_report_configs;

CREATE TABLE cth_users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),
    role VARCHAR(50) DEFAULT 'ROLE_USER',
    account_non_locked BIT DEFAULT 1 NOT NULL,
    enabled BIT DEFAULT 1 NOT NULL,
    failed_attempt INT DEFAULT 0 NOT NULL,
    password_last_changed DATETIME2,
    account_created DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE cth_report_configs (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    report_name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),
    query_sql VARCHAR(2000) NOT NULL,
    target_table VARCHAR(255),
    created_date DATETIME2 DEFAULT GETDATE()
);

-- Seed Default Admin User
INSERT INTO cth_users (username, password, full_name, email, phone, role, account_non_locked, enabled, failed_attempt, password_last_changed)
VALUES ('admin', '$2a$10$e0MYzXyjpJS7Pd0RVvHwHe11.75X67/7.794m/90yM6v0y4/54432', 'System Administrator', 'admin@cth.com', '+1234567890', 'ROLE_ADMIN', 1, 1, 0, GETDATE());

-- Seed Default Online Report
INSERT INTO cth_report_configs (report_name, description, query_sql, target_table)
VALUES ('User Activity & Status Report', 'List of all registered users and account status', 'SELECT username, full_name, email, role, account_non_locked, enabled, failed_attempt FROM cth_users', 'cth_users');
