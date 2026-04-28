package com.genius.utils.lib;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.genius.utils.model.ApiToken;
import com.genius.utils.model.App;
import com.genius.utils.model.JwtUser;
import com.genius.utils.model.VerifyApiToken;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthHandler {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int IV_LENGTH = 16;
    private static final String SECRET_KEY = "OhMyGenius!";
    private static final long TIME_LIMIT =  15L * 60 * 1000; // 15 minutes, 24L * 60 * 60 * 1000 = 1 day

    private static final long JWT_ACCESS_EXPIRED_MIN = 15;
    private static final long JWT_REFRESH_EXPIRED_DAY = 7;

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

    public static String getJwtSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
    
    public static String getRefreshSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
    
    public static String getApiSecret() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static Map<String, Object> generateJwtToken(JwtUser user) {
        return generateJwtToken(user, null, null);
    }
    
    public static Map<String, Object> generateJwtToken(JwtUser user, String jwtSecret, String refreshSecret) {
        Map<String, Object> result = new HashMap<>();
        
        if (user == null || user.getId() == null) {
            result.put("status", 401);
            result.put("message", "Invalid user provided!");
            return result;
        }

        App app = MediaHandler.readJSON("config/app.config.json", new TypeReference<App>() {});     
        String JWT_SECRET = (jwtSecret != null) ? jwtSecret : app.getJwtSecret();
        String REFRESH_SECRET = (refreshSecret != null) ? refreshSecret : app.getRefreshSecret();
        
        if (JWT_SECRET == null || REFRESH_SECRET == null) {
            result.put("status", 401);
            result.put("message", "JWT_SECRET and REFRESH_SECRET must be configured!");
            return result;
        }
        
        // Generate Access Token
        String accessToken = generateAccessToken(user, JWT_SECRET);
        
        // Generate Refresh Token
        String refreshToken = generateRefreshToken(user, REFRESH_SECRET);
        
        Map<String, Object> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        
        result.put("status", 200);
        result.put("tokens", tokens);
        
        return result;
    }

    private static String generateAccessToken(JwtUser user, String secret) {
        Instant now = Instant.now();
        Instant expiration = now.plus(JWT_ACCESS_EXPIRED_MIN, ChronoUnit.MINUTES);
        
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("userId", user.getId())
                .claim("username", user.getUsername())
                .claim("role", user.getRole() != null ? user.getRole() : "user")
                .claim("type", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
    
    private static String generateRefreshToken(JwtUser user, String secret) {
        Instant now = Instant.now();
        Instant expiration = now.plus(JWT_REFRESH_EXPIRED_DAY, ChronoUnit.DAYS);
        
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("userId", user.getId())
                .claim("type", "refresh")
                .claim("version", user.getTokenVersion() != null ? user.getTokenVersion() : 1)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public static Map<String, Object> verifyJwtToken(String token, String secret) {
        Map<String, Object> result = new HashMap<>();
        
        if (token == null || token.isEmpty()) {
            result.put("status", 401);
            result.put("message", "Invalid token provided!");
            return result;
        }
        
        if (secret == null || secret.isEmpty()) {
            result.put("status", 401);
            result.put("message", "Invalid secret provided!");
            return result;
        }
        
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            result.put("status", 200);
            result.put("message", "JWT verified.");
            result.put("decoded", claims);
            
            return result;
        } catch (ExpiredJwtException e) {
            result.put("status", 403);
            result.put("message", "Token expired!");
            result.put("expired", true);
            return result;
        } catch (MalformedJwtException | SignatureException | IllegalArgumentException e) {
            result.put("status", 401);
            result.put("message", "Invalid token!");
            return result;
        }
    }
}
