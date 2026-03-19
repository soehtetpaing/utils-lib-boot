package com.genius.utils;

import com.genius.utils.lib.CommonHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonMain {
    public static void main(String[] args) {
        log.info("syskey: " + CommonHandler.getSyskey());
        log.info("Super Admin: " + CommonHandler.getSuperAdmin());
        log.info("Super Admin: " + CommonHandler.toJSON(CommonHandler.getSuperAdmin()));
        log.info("Device Info: " + CommonHandler.getDeviceInfo());
        log.info("Device Info: " + CommonHandler.toJSON(CommonHandler.getDeviceInfo()));
        log.info("Demo User: " + CommonHandler.getDemoUser());
        log.info("Demo User: " + CommonHandler.toJSON(CommonHandler.getDemoUser()));
    }
}
