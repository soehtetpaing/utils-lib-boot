package com.genius.utils;

import java.util.LinkedHashMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genius.utils.lib.CommonHandler;
import com.genius.utils.lib.MediaHandler;
import com.genius.utils.lib.MigrateHandler;
import com.genius.utils.model.App;
import com.genius.utils.model.DatabaseInfo;
import com.genius.utils.model.MigrateInfo;
import com.genius.utils.model.MigrateResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MigrateMain {
    public static void main(String[] args) {
        App app = MediaHandler.readJSON("config/app.config.json", new TypeReference<App>() {});
        LinkedHashMap<String, Object> rawdb = MediaHandler.readJSON(
            "config/database.config.json", 
            new TypeReference<LinkedHashMap<String, Object>>() {});
        
        String env = "dev"; // or "prod"
        ObjectMapper mapper = new ObjectMapper();
        LinkedHashMap<String, Object> database = mapper.convertValue(
            rawdb.get(env), 
            new TypeReference<LinkedHashMap<String, Object>>() {});

        DatabaseInfo databaseInfo = new DatabaseInfo();
        databaseInfo.setSecretKey(app.getSecretKey());
        databaseInfo.setType(database.get("type").toString());
        databaseInfo.setHost(database.get("host").toString());
        databaseInfo.setPort(Long.parseLong(database.get("port").toString()));
        databaseInfo.setUsername(database.get("username").toString());
        databaseInfo.setPasswordHex(database.get("passwordHex").toString());
        databaseInfo.setDatabase(database.get("database").toString());
        databaseInfo.setEncrypt(false);
        databaseInfo.setTrustServerCertificate(true);

        MigrateInfo migrateInfo = new MigrateInfo();
        migrateInfo.setName("Utils");
        migrateInfo.setVersion("1.1.0"); // major.minor.patch
        migrateInfo.setDepth(2); // compare major.minor

        MigrateResult result = MigrateHandler.migrateVersion(databaseInfo, migrateInfo);
        log.info(CommonHandler.toJSON(result));
    }
    
}
