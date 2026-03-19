package com.genius.utils;

import com.genius.utils.lib.AuthHandler;
import com.genius.utils.lib.LogHandler;
import com.genius.utils.model.ApiToken;
import com.genius.utils.model.LogInfo;
import com.genius.utils.model.VerifyApiToken;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;


@Slf4j
public class LogMain {
    public static void main(String[] args) throws IOException {
        LogInfo logInfo = new LogInfo("logs", "TokenLog", "GeniusUtils", "login", "verifyApiToken", "genius.iq");
        ApiToken apiToken = AuthHandler.generateApiToken("", "");
        VerifyApiToken verified = AuthHandler.verifyApiToken(apiToken.getToken(), "ms365", "microsoft");
        LogHandler.log(logInfo, apiToken, verified, "");
    }
}
