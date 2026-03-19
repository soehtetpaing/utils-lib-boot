package com.genius.utils.lib;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.genius.utils.model.ApiToken;
import com.genius.utils.model.VerifyApiToken;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthHandler {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int IV_LENGTH = 16;
    private static final String SECRET_KEY = "OhMyGenius!";
    private static final long TIME_LIMIT =  15L * 60 * 1000; // 15 minutes, 24L * 60 * 60 * 1000 = 1 day

    // get uuid by Genius iQ @20251107
    public static String getUniqueId() {
        return NanoIdUtils.randomNanoId(new SecureRandom(), NanoIdUtils.DEFAULT_ALPHABET, 11);
    }

    // encrypt & decrypt data by Genius iQ @20251108 modified @20260319
    public static String encrypt(String plainText, String secretKey) {
        try {
            String secret = (secretKey != null && !secretKey.isBlank()) ? secretKey : SECRET_KEY;
            SecretKeySpec keySpec = deriveKey(secret);
            
            byte[] ivBytes = new byte[IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(ivBytes);
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            
            byte[] ciphertextBytes = cipher.doFinal(
                plainText.getBytes(StandardCharsets.UTF_8)
            );
            
            byte[] combined = new byte[ivBytes.length + ciphertextBytes.length];
            System.arraycopy(ivBytes, 0, combined, 0, ivBytes.length);
            System.arraycopy(ciphertextBytes, 0, combined, ivBytes.length, ciphertextBytes.length);
            
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            return null;
        }
    }

    public static String decrypt(String encryptedText, String secretKey) {        
        try {
            String secret = (secretKey != null && !secretKey.isBlank()) ? secretKey : SECRET_KEY;
            SecretKeySpec keySpec = deriveKey(secret);
            
            byte[] combined = Base64.getDecoder().decode(encryptedText);
            
            byte[] ivBytes = new byte[IV_LENGTH];
            byte[] ciphertextBytes = new byte[combined.length - IV_LENGTH];
            
            System.arraycopy(combined, 0, ivBytes, 0, IV_LENGTH);
            System.arraycopy(combined, IV_LENGTH, ciphertextBytes, 0, ciphertextBytes.length);
            
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            
            byte[] decryptedBytes = cipher.doFinal(ciphertextBytes);
            
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    // APIs token authorization by Genius iQ @20251108 modified @20251115
    public static ApiToken generateApiToken(String secretKey, String domain) {
        try {
            String secretkey = (secretKey != null && !secretKey.isBlank()) ? secretKey : SECRET_KEY;
            String domainUse = (domain != null && !domain.isBlank()) ? domain : "*";

            long expireTime =  new Date().getTime() + TIME_LIMIT;
            String uuid = getUniqueId();
            String plainText = domainUse + uuid + expireTime + secretkey;

            byte[] secret = MessageDigest.getInstance("SHA-256").digest(plainText.getBytes(StandardCharsets.UTF_8));

            String encodedHash = bytesToHex(secret);
            String domainHex = bytesToHex(domainUse.getBytes(StandardCharsets.UTF_8));
            String uuidHex = bytesToHex(uuid.getBytes(StandardCharsets.UTF_8));
            String expireHex = Long.toHexString(expireTime);

            String token = uuidHex + "." + encodedHash + "." + expireHex + "." + domainHex;
            String expireAt = DateTimeHandler.formatTokenExpireTime(expireTime);

            return new ApiToken(token, expireAt);
        } catch (Exception e) {
            return null;
        }
    }

    public static VerifyApiToken verifyApiToken(String token, String secretKey, String domain) {
        try {
            String secretkey = (secretKey != null && !secretKey.isBlank()) ? secretKey : SECRET_KEY;
            String domainCheck = (domain != null && !domain.isBlank()) ? domain : "*";

            String[] parts = token.split("\\.");
            if (parts.length != 4) {
                return new VerifyApiToken(400, "Invalid Token Format!");
            }

            String uuidHex = parts[0];
            String tokenHash = parts[1];
            String expireHex = parts[2];
            String domainHex = parts[3];            

            String domainUse = new String(hexToBytes(domainHex), StandardCharsets.UTF_8);
            String uuid = new String(hexToBytes(uuidHex), StandardCharsets.UTF_8);
            long expireTime = Long.parseLong(expireHex, 16);

            if (new Date().getTime() > expireTime) {
                return new VerifyApiToken(403, "Token Expired!");
            }

            if (!domainUse.equals(domainCheck)) {
                return new VerifyApiToken(401, "Unauthorized Domain: " + domainCheck + "!");
            }

            String plainText = domainUse + uuid + expireTime + secretkey;
            byte[] secret = MessageDigest.getInstance("SHA-256").digest(plainText.getBytes(StandardCharsets.UTF_8));
            String expectedHash = bytesToHex(secret);

            if (!expectedHash.equals(tokenHash)) {
                return new VerifyApiToken(500, "Invalid Token!");
            }

            return new VerifyApiToken(200, "Token Verification Success.");            
        } catch (Exception e) {
            return new VerifyApiToken(500, "Token Verification Failed!");
        }
    }

    // private methods by Genius iQ @20251109
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    private static SecretKeySpec deriveKey(String secret) throws Exception {
        MessageDigest sha = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] keyBytes = sha.digest(secret.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    }
}
