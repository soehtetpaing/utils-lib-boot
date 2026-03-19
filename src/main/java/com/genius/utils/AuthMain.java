package com.genius.utils;

import com.genius.utils.lib.AuthHandler;
import com.genius.utils.lib.CommonHandler;
import com.genius.utils.model.ApiToken;
import com.genius.utils.model.VerifyApiToken;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthMain {
    public static void main(String[] args) {   
        log.info("UUID: " + AuthHandler.getUniqueId());
        log.info("Encrypted: " + AuthHandler.encrypt("123", null));
        log.info("Decrypted: " + AuthHandler.decrypt("dzg7xj1zIyhvyRx0l3EL4muepulIdcfiplxRYe7ug34=", null));
        log.info("Decrypted (Wrong Secret): " + AuthHandler.decrypt("dzg7xj1zIyhvyRx0l3EL4muepulIdcfiplxRYe7ug34=", "ms365"));
        log.info("Encrypted: " + AuthHandler.encrypt("@dminP@ssw0rd", "ms365"));
        log.info("Decrypted: " + AuthHandler.decrypt("3IQyFcVt8K1Y1OE0dmaaEbbLIBVgGFam36Ok/MEVEEI=", "ms365"));
        log.info("Decrypted (Wrong Secret): " + AuthHandler.decrypt("3IQyFcVt8K1Y1OE0dmaaEbbLIBVgGFam36Ok/MEVEEI=", ""));

        ApiToken apiToken = AuthHandler.generateApiToken(null, null);
        // log.info("API Token: " + apiToken);
        log.info("API Token: " + CommonHandler.toJSON(apiToken));
        VerifyApiToken verified = AuthHandler.verifyApiToken(apiToken.getToken(), null, null);
        log.info("Verify API Token: " + CommonHandler.toJSON(verified));
        verified = AuthHandler.verifyApiToken("4b46727046666b62697432.fd83c0a70de325863af7236d93fd29e7be463a0802588d0a72813383de6b66bf.19ada4b7965.2a", "ms365", "microsoft");
        log.info("Verify API Token (Wrong Secret): " + CommonHandler.toJSON(verified));
    }
}
