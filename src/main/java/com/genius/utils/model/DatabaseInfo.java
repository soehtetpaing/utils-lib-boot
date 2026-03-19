package com.genius.utils.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseInfo {
    private String url;
    private String secretKey;
    private String type;
    private String host;
    private Long port;
    private String username;
    private String passwordHex;
    private String database;
    private boolean encrypt; // mssql
    private boolean trustServerCertificate; // mssql
    private String sslMode; // postgre
    private String sslRootCert; // postgre
    private boolean useSSL; // mysql
    private boolean allowPublicKeyRetrieval; // mysql
    private String serverTimezone; // mysql
}
