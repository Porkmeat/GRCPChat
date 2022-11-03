/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.grpcchatapp;

import com.chatapp.service.LoginService;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.json.JSONObject;

/**
 *
 * @author Mariano
 */
public class JWToken {

    private final String JWT_HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private final String secret = "testkey";
    private final String encodedHeader;
    private final String encodedPayload;
    private final String encodedSignature;

    public JWToken(String username, int userId) {
        this.encodedHeader = encode(JWT_HEADER.getBytes());
        this.encodedPayload = generatePayload(username, userId);
        this.encodedSignature = hmacSha256(encodedHeader + "." + encodedPayload, secret);
    }

    public JWToken(String incomingToken) {
        String[] tokenParts = incomingToken.split("\\.");
        this.encodedHeader = tokenParts[0];
        this.encodedPayload = tokenParts[1];
        this.encodedSignature = tokenParts[2];
    }

    private String generatePayload(String username, int userId) {
        JSONObject payload = new JSONObject();
        payload.put("sub", userId);
        payload.put("name", username);
        payload.put("iat", Instant.now().toString());

        return encode(payload.toString().getBytes());
    }

    private String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String decode(String encodedString) {
        return new String(Base64.getUrlDecoder().decode(encodedString));
    }

    private String hmacSha256(String data, String secret) {
        try {
            byte[] hash = secret.getBytes(StandardCharsets.UTF_8);
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(hash, "HmacSHA256");
            sha256Hmac.init(secretKey);

            byte[] signedBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            return encode(signedBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            Logger.getLogger(LoginService.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return null;
        }
    }
    
    public boolean isValid() {
        String expectedSignature = hmacSha256(encodedHeader + "." + encodedPayload, secret);
        return expectedSignature.equals(encodedSignature);
    }

    public int getUserId() {
         JSONObject payload = new JSONObject(decode(encodedPayload));
        return payload.getInt("sub");
    }
    
    public String getUsername() {
        JSONObject payload = new JSONObject(decode(encodedPayload));
        return payload.getString("name");
    }
    
    @Override
    public String toString() {
        return encodedHeader + "." + encodedPayload + "." + encodedSignature;
    }

}
