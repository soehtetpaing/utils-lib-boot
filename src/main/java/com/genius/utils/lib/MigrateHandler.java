package com.genius.utils.lib;

import java.io.StringReader;
import java.sql.Driver;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.genius.utils.model.App;
import com.genius.utils.model.DatabaseInfo;
import com.genius.utils.model.DeviceInfo;
import com.genius.utils.model.LogInfo;
import com.genius.utils.model.MigrateInfo;
import com.genius.utils.model.MigrateResult;
import com.genius.utils.model.MigrationDetail;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MigrateHandler {
    private static JdbcTemplate jdbc;
    private static String projectName;
    private static String requestBy;
    private static LogInfo logInfo;
    private static MigrateResult result;
    
    public static MigrateResult migrateVersion(DatabaseInfo databaseInfo, MigrateInfo migrateInfo) {
        result = new MigrateResult();
        
        App app = MediaHandler.readJSON("meta/app.json", new TypeReference<App>() {});
        projectName = app.getName() + "@" + app.getVersion();
        
        DeviceInfo deviceInfo = CommonHandler.getDeviceInfo();
        if (migrateInfo == null) {
            requestBy = deviceInfo.getHostname() + "@" + deviceInfo.getUserInfo().getUsername();
        } else {
            requestBy = migrateInfo.getName() + "@" + migrateInfo.getVersion();
        }

        logInfo = new LogInfo();
        logInfo.setFilepath("logs");
        logInfo.setFilename("VersionMigrateLog");
        logInfo.setProjectName(projectName);
        logInfo.setRequestBy(requestBy);

        try {
            // create db IF NOT EXISTS
            init(databaseInfo);
            // share jdbc databse connection
            connect(databaseInfo);
            // create vac001, vac002 IF NOT EXISTS
            create(jdbc, databaseInfo);

            String databaseType = databaseInfo.getType().toLowerCase();
            // vac001.t1, default 1.0.0
            String migratedVersion = getMigratedVersion(jdbc, databaseType);

            // logging setup
            logInfo.setClassName("Migration");
            logInfo.setMethodName("migrateVersion");

            String version = migrateInfo.getVersion();
            int depth = migrateInfo.getDepth();
            int compare  = compareVersions(version, migratedVersion, depth);
            if (compare <= 0) {
                // logging
                result.setStatus(201);
                result.setMessage("No need to migrate!");
                LogHandler.log(logInfo, migrateInfo, result, "");
                return result;
            }

            // insert data to version migration hdr
            long hdrSyskey = CommonHandler.getSyskey();
            String createdDate = DateTimeHandler.getMyanmarDateTime();
            String createdUser = deviceInfo.getHostname() + '@' + deviceInfo.getUserInfo().getUsername();
            String query = "INSERT INTO vac001 (syskey, createddate, createduser, t1) VALUES (?, ?, ?, ?)";
            jdbc.update(query, ps -> {
                ps.setLong(1, hdrSyskey);
                ps.setString(2, createdDate);
                ps.setString(3, createdUser);
                ps.setString(4, version);
            });

            int[] ver = Arrays.stream(migratedVersion.split("\\."))
                    .mapToInt(s -> s.isBlank() ? 0 : Integer.parseInt(s))
                    .toArray();
            while (compare > 0) {
                if (ver.length < depth) {
                    ver = Arrays.copyOf(ver, depth);
                }
                ver[depth - 1] = (ver[depth - 1]) + 1;
                Arrays.fill(ver, depth, ver.length, 0);

                String fileVer = Arrays.stream(ver)
                            .mapToObj(String::valueOf)
                            .collect(Collectors.joining("."));

                // check sql file exists
                String SQL_FILENAME = databaseType + "_" 
                                    + migrateInfo.getName().replace(' ', '_').toLowerCase()
                                    + "_v" + fileVer + ".sql";
                String SQL_FILE = "migration/" + SQL_FILENAME;
                boolean FILE_EXIST = MediaHandler.mediaExists(SQL_FILE);

                if (!FILE_EXIST) {
                    // logging
                    result.setStatus(404);
                    result.setMessage(".\\migration\\" + SQL_FILENAME + " does not exist!");
                    LogHandler.log(logInfo, migrateInfo, result, "");
                    return result;
                }

                // insert data to version migration dtl        
                String TEXT = MediaHandler.readText(SQL_FILE);
                List<String> QUERIES = Arrays.stream(TEXT.split("(?i);\\s*(?=\\n|$)"))
                                    .map(String::trim)
                                    .filter(q -> !q.isEmpty())
                                    .toList();

                for (String qry : QUERIES) {
                    try {
                        jdbc.execute(qry);

                        MigrationDetail dtl = new MigrationDetail();
                        dtl.setSyskey(CommonHandler.getSyskey());
                        dtl.setCreatedDate(DateTimeHandler.getMyanmarDateTime());
                        dtl.setCreatedUser(createdUser);
                        dtl.setScriptName(SQL_FILENAME);
                        dtl.setStatus(200);
                        dtl.setMessage("");
                        dtl.setQuery(qry);
                        dtl.setParentId(hdrSyskey);

                        insertDetail(jdbc, dtl);

                    } catch (Exception err) {
                        String message = err.getMessage();
                        if (message == null) message = "Unknown error";
                        int idx = message.indexOf(" [");
                        if (idx > 0) message = message.substring(0, idx);
                        if (message.length() > 250) message = message.substring(0, 250);

                        MigrationDetail dtl = new MigrationDetail();
                        dtl.setSyskey(CommonHandler.getSyskey());
                        dtl.setCreatedDate(DateTimeHandler.getMyanmarDateTime());
                        dtl.setCreatedUser(createdUser);
                        dtl.setScriptName(SQL_FILENAME);
                        dtl.setStatus(400);
                        dtl.setMessage(message);
                        dtl.setQuery(qry);
                        dtl.setParentId(hdrSyskey);

                        insertDetail(jdbc, dtl);
                    }
                }

                compare--;
            }

            // logging
            result.setStatus(200);
            result.setMessage("Version migration success.");
            LogHandler.log(logInfo, migrateInfo, result, "");

            return result;  

        } catch (Exception err) {
            log.info("Error: ", err);
            
            // logging
            logInfo.setClassName("Migration");
            logInfo.setMethodName("migrateVersion");
            result.setStatus(500);
            result.setMessage("Migrate version fail!");
            LogHandler.log(logInfo, migrateInfo, result, err.getMessage());
            return result;
        }
    }

    private static void init(DatabaseInfo databaseInfo) {
        logInfo.setClassName("Connection");
        logInfo.setMethodName("create");
        result.setMessage("Connection create fail!");

        try {
            if (databaseInfo == null) {
                LogHandler.log(logInfo, databaseInfo, result, "Empty database info!");
                return;
            }

            String url = "";
            String checkQuery = "";
            String createQuery = "";

            switch (databaseInfo.getType().toLowerCase()) {
                case "mssql" -> {
                    url = "jdbc:sqlserver://" + databaseInfo.getHost() + ":" + databaseInfo.getPort() 
                        + ";databaseName=master" 
                        + ";encrypt=" + databaseInfo.isEncrypt()
                        + ";trustServerCertificate=" + databaseInfo.isTrustServerCertificate();
                    checkQuery = "SELECT name FROM sys.databases WHERE name = ?";
                    createQuery = "CREATE DATABASE [" + databaseInfo.getDatabase() + "]";
                }
                case "postgre", "pg" -> {
                    url = "jdbc:postgresql://" + databaseInfo.getHost() + ":" + databaseInfo.getPort() + "/postgres";
                    checkQuery = "SELECT 1 FROM pg_database WHERE datname = ?";
                    createQuery = "CREATE DATABASE \"" + databaseInfo.getDatabase() + "\"";
                }
                case "mysql" -> {
                    url = "jdbc:mysql://" + databaseInfo.getHost() + ":" + databaseInfo.getPort() + "/mysql"
                        + "?useSSL=" + databaseInfo.isUseSSL()
                        + "&allowPublicKeyRetrieval=" +databaseInfo.isAllowPublicKeyRetrieval()
                        + "true&serverTimezone=" + databaseInfo.getServerTimezone();
                    checkQuery = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?";
                    createQuery = "CREATE DATABASE IF NOT EXISTS `" + databaseInfo.getDatabase() + "`";
                }
            }

            JdbcTemplate tmp = new JdbcTemplate(driverDataSource(url, databaseInfo));
            List<Map<String, Object>> exists = tmp.queryForList(checkQuery, databaseInfo.getDatabase());

            if (exists.isEmpty()) {
                tmp.execute(createQuery);
            }

        } catch (Exception err) {
            // logging
            result.setStatus(400);
            result.setMessage("Database create fail!");
            LogHandler.log(logInfo, databaseInfo, result, err.getMessage());
            return;
        }      
    }

    private static SimpleDriverDataSource driverDataSource(String url, DatabaseInfo databaseInfo) throws Exception {
        Driver driver = switch (databaseInfo.getType().toLowerCase()) {
            case "mssql" -> (Driver) Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").getDeclaredConstructor().newInstance();
            case "postgre", "pg" -> (Driver) Class.forName("org.postgresql.Driver").getDeclaredConstructor().newInstance();
            case "mysql" -> (Driver) Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            default -> throw new IllegalArgumentException("Unsupported database type: " + databaseInfo.getType());

        };

        String password = AuthHandler.decrypt(databaseInfo.getPasswordHex(), databaseInfo.getSecretKey());
        return new SimpleDriverDataSource(driver, url, databaseInfo.getUsername(), password);
    }

    private static void connect(DatabaseInfo databaseInfo) throws Exception {
        String url = switch (databaseInfo.getType().toLowerCase()) {
            case "mssql" -> "jdbc:sqlserver://" + databaseInfo.getHost() + ":" + databaseInfo.getPort() 
                            + ";databaseName=" + databaseInfo.getDatabase()
                            + ";encrypt=" + databaseInfo.isEncrypt()
                            + ";trustServerCertificate=" + databaseInfo.isTrustServerCertificate();
            case "postgre", "pg" -> "jdbc:postgresql://" + databaseInfo.getHost() + ":" + databaseInfo.getPort() + "/" + databaseInfo.getDatabase();
            case "mysql" -> "jdbc:mysql://" + databaseInfo.getHost() + ":" + databaseInfo.getPort() + "/" + databaseInfo.getDatabase()
                            + "?useSSL=" + databaseInfo.isUseSSL()
                            + "&allowPublicKeyRetrieval=" +databaseInfo.isAllowPublicKeyRetrieval()
                            + "true&serverTimezone=" + databaseInfo.getServerTimezone();
            default -> throw new IllegalArgumentException("Unsupported database type: " + databaseInfo.getType());
        };

        jdbc = new JdbcTemplate(driverDataSource(url, databaseInfo));

        // logging
        logInfo.setClassName("Connection");
        logInfo.setMethodName("connect");
        result.setStatus(200);
        result.setMessage("Connected.");
        LogHandler.log(logInfo, databaseInfo, result, "");
    }

    private static void create(JdbcTemplate jdbc, DatabaseInfo databaseInfo) {
        logInfo.setClassName("Connection");
        logInfo.setMethodName("create");

        try {
            String query = "";

            switch (databaseInfo.getType().toLowerCase()) {
                case "mssql" -> {
                    query = """
                                IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='vac001' AND xtype='U')
                                CREATE TABLE vac001 (
                                    syskey BIGINT NOT NULL DEFAULT 0,
                                    autokey BIGINT IDENTITY(1,1) NOT NULL,
                                    createddate VARCHAR(50) NOT NULL DEFAULT '',
                                    createduser VARCHAR(50) DEFAULT '',
                                    t1 VARCHAR(50) NOT NULL DEFAULT '',
                                    PRIMARY KEY (syskey),
                                );

                                IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='vac002' AND xtype='U')
                                CREATE TABLE vac002 (
                                    syskey BIGINT NOT NULL DEFAULT 0,
                                    autokey BIGINT IDENTITY(1,1) NOT NULL,
                                    createddate VARCHAR(50) NOT NULL DEFAULT '',
                                    createduser VARCHAR(50) DEFAULT '',
                                    t1 VARCHAR(255) NOT NULL DEFAULT '',
                                    n1 INT DEFAULT 0,
                                    t2 VARCHAR(255) NOT NULL DEFAULT '',
                                    t3 NVARCHAR(MAX) NOT NULL DEFAULT '',
                                    n2 BIGINT DEFAULT 0,
                                    PRIMARY KEY (syskey),
                                );
                            """;
                }
                case "postgre", "pg" -> {
                    query = """
                                CREATE TABLE IF NOT EXISTS vac001 (
                                    syskey BIGINT NOT NULL DEFAULT 0,
                                    autokey BIGSERIAL NOT NULL,
                                    createddate VARCHAR(50) NOT NULL DEFAULT '',
                                    createduser VARCHAR(50) DEFAULT '',
                                    t1 VARCHAR(50) NOT NULL DEFAULT '',
                                    PRIMARY KEY (syskey)
                                );

                                CREATE TABLE IF NOT EXISTS vac002 (
                                    syskey BIGINT NOT NULL DEFAULT 0,
                                    autokey BIGSERIAL NOT NULL,
                                    createddate VARCHAR(50) NOT NULL DEFAULT '',
                                    createduser VARCHAR(50) DEFAULT '',
                                    t1 VARCHAR(255) NOT NULL DEFAULT '',
                                    n1 INT DEFAULT 0,
                                    t2 VARCHAR(255) NOT NULL DEFAULT '',
                                    t3 TEXT NOT NULL DEFAULT '',
                                    n2 BIGINT DEFAULT 0,
                                    PRIMARY KEY (syskey)
                                );
                            """;
                }
                case "mysql" -> {
                    query = """
                                CREATE TABLE IF NOT EXISTS vac001 (
                                    syskey BIGINT NOT NULL DEFAULT 0,
                                    autokey BIGINT NOT NULL AUTO_INCREMENT,
                                    createddate VARCHAR(50) NOT NULL DEFAULT '',
                                    createduser VARCHAR(50) DEFAULT '',
                                    t1 VARCHAR(50) NOT NULL DEFAULT '',
                                    PRIMARY KEY (syskey),
                                    UNIQUE KEY (autokey)
                                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

                                CREATE TABLE IF NOT EXISTS vac002 (
                                    syskey BIGINT NOT NULL DEFAULT 0,
                                    autokey BIGINT NOT NULL AUTO_INCREMENT,
                                    createddate VARCHAR(50) NOT NULL DEFAULT '',
                                    createduser VARCHAR(50) DEFAULT '',
                                    t1 VARCHAR(255) NOT NULL DEFAULT '',
                                    n1 INT DEFAULT 0,
                                    t2 VARCHAR(255) NOT NULL DEFAULT '',
                                    t3 TEXT NOT NULL,
                                    n2 BIGINT DEFAULT 0,
                                    PRIMARY KEY (syskey),
                                    UNIQUE KEY (autokey)
                                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                            """;
                }
            }

            jdbc.execute(query);

        } catch (Exception err) {
            // logging
            result.setStatus(400);
            result.setMessage("Migrate table create fail!");
            LogHandler.log(logInfo, databaseInfo, result, err.getMessage());
            return;
        }

    }

    private static String getMigratedVersion(JdbcTemplate jdbc, String databaseType) {
        try {
            String query = "";

            switch (databaseType) {
                case "mssql" -> query = "SELECT TOP 1 t1 FROM vac001 ORDER BY autokey DESC";
                case "postgre", "pg" -> query = "SELECT t1 FROM vac001 ORDER BY autokey DESC LIMIT 1";
                case "mysql" -> query = "SELECT t1 FROM vac001 ORDER BY autokey DESC LIMIT 1";
            }

            String version = jdbc.queryForObject(query, String.class);

            return version != null ? version : "1.0.0";
        } catch (Exception err) {
            return "1.0.0";
        }
    }

    private static void insertDetail(JdbcTemplate jdbc, MigrationDetail dtl) {
        logInfo.setClassName("Migration");
        logInfo.setMethodName("insertDetail");

        try {
            String query = "INSERT INTO vac002 (syskey, createddate, createduser, t1, n1, t2, t3, n2) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            jdbc.update(query, ps -> {
                ps.setLong(1, dtl.getSyskey());
                ps.setString(2, dtl.getCreatedDate());
                ps.setString(3, dtl.getCreatedUser());
                ps.setString(4, dtl.getScriptName());
                ps.setInt(5, dtl.getStatus());
                ps.setString(6, dtl.getMessage());
                ps.setCharacterStream(7, new StringReader(dtl.getQuery()), dtl.getQuery().length());
                ps.setLong(8, dtl.getParentId());
            });

        } catch (Exception err) {
            // logging
            result.setStatus(500);
            result.setMessage("Version migration crash!");
            LogHandler.log(logInfo, dtl, result, err.getMessage());
            return;
        }
    }

    private static int compareVersions(String version, String migratedVersion, int depth) {
        if (version == null || version.isBlank()) version = "";
        if (migratedVersion == null || migratedVersion.isBlank()) migratedVersion = "";
        if (depth <= 0) depth = 3;

        int[] v1 = Arrays.stream(version.split("\\."))
                .mapToInt(s -> s.isBlank() ? 0 : Integer.parseInt(s))
                .toArray();

        int[] v2 = Arrays.stream(migratedVersion.split("\\."))
                .mapToInt(s -> s.isBlank() ? 0 : Integer.parseInt(s))
                .toArray();

        // 1.0.21, 1.0.0, 2 => 1 - 1 = 0, 0 - 0 = 0
        // 1.21.22, 1.19.0, 2 => 1 - 1 = 0, 21 - 19 = 2
        // 1.21.22, 1.21.5, 3 => 1 - 1 = 0, 21 - 21 = 0, 22 - 5 = 17
        // 1.0.21, 1.2.0, 2 => 1 - 1 = 0, 0 - 2 = -2
        for (int i = 0; i < depth; i++) {
            int a = (i < v1.length) ? v1[i] : 0;
            int b = (i < v2.length) ? v2[i] : 0;

            int diff = a - b;
            if (diff != 0) return diff;
        }

        return 0;
    }

}
