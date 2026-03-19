package com.genius.utils.lib;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genius.utils.model.DeviceInfo;
import com.genius.utils.model.DeviceInfo.UserInfo;
import com.genius.utils.model.User;

@Slf4j
public class CommonHandler {
    // get syskey by Genius iQ @20250508
    public static long getSyskey() {
        return Long.parseLong(DateTimeHandler.getMyanmarTimestamp().substring(2, 17));
    }

    // get super admin by Genius iQ @20250515
    public static User getSuperAdmin() {
        User superAdmin = new User();
        superAdmin.setSyskey( 1);
        superAdmin.setAutokey(1);
        superAdmin.setCreatedDate(DateTimeHandler.getMyanmarDateTime());
        superAdmin.setModifiedDate(DateTimeHandler.getMyanmarDateTime());
        superAdmin.setCreatedUser("sa");
        superAdmin.setModifiedUser("sa");
        superAdmin.setRecordStatus(1);
        superAdmin.setUserId("genius.iq");
        superAdmin.setUserName("Soe Htet Paing");
        superAdmin.setUuid(AuthHandler.getUniqueId());
        superAdmin.setPassword(AuthHandler.encrypt("123", "OhMyGenius!"));
        superAdmin.setPincode(AuthHandler.encrypt("123", "OhMyGenius!"));

        return superAdmin;
    }

    // Object to JSON by Genius iQ @20250515 modified @20251120
    public static String toJSON(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception err) {
            return "{}";
        }
    }

    // get device info by Genius iQ @20251108
    public static DeviceInfo getDeviceInfo() {
        DeviceInfo deviceInfo = new DeviceInfo();
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            deviceInfo.setHostname(localHost.getHostName());
            deviceInfo.setIpAddress(localHost.getHostAddress());
        } catch (Exception err) {
            deviceInfo.setHostname("Unknown");
            deviceInfo.setIpAddress("Unknown");
        }

        deviceInfo.setUserInfo(new UserInfo(System.getProperty("user.name"), System.getProperty("user.home")));

        return deviceInfo;
    }

    // get demo user by Genius iQ @20251109
    public static User getDemoUser() {
        User demoUser = new User();
        demoUser.setSyskey(getSyskey());
        demoUser.setAutokey(2);
        demoUser.setCreatedDate(DateTimeHandler.getMyanmarDateTime());
        demoUser.setModifiedDate(DateTimeHandler.getMyanmarDateTime());
        demoUser.setCreatedUser("genius.iq");
        demoUser.setModifiedUser("genius.iq");
        demoUser.setRecordStatus(1);
        demoUser.setUserId("demo");
        demoUser.setUserName("Demo User");
        demoUser.setUuid(AuthHandler.getUniqueId());
        demoUser.setPassword(AuthHandler.encrypt("", "OhMyGenius!"));
        demoUser.setPincode(AuthHandler.encrypt("123", "OhMyGenius!"));

        return demoUser;
    }
}
