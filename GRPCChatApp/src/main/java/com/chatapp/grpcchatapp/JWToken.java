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
 * Object to generate and decode JWTokens.
 *
 * @author Mariano Cuneo
 */
public class JWToken {

    private final String JWT_HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private final String secret = "testkey";
    private final String encodedHeader;
    private final String encodedPayload;
    private final String encodedSignature;

    /**
     * Default Class Constructor. This constructor is used to generate a new
     * JWToken after a successful login.
     *
     * @param username successful login's username.
     * @param userId successful login's user ID.
     */
    public JWToken(String username, int userId) {
        this.encodedHeader = encode(JWT_HEADER.getBytes());
        this.encodedPayload = generatePayload(username, userId);
        this.encodedSignature = hmacSha256(encodedHeader + "." + encodedPayload, secret);
    }

    /**
     * Class constructor from encoded String. This constructor is used to
     * generate a JWToken Object from a client provided token String. If the
     * provided String structure is not as expected, generates a failed token.
     *
     * @param incomingToken encoded string provided by client.
     */
    public JWToken(String incomingToken) {
        String[] tokenParts = incomingToken.split("\\.");
        if (tokenParts.length == 3) {
            this.encodedHeader = tokenParts[0];
            this.encodedPayload = tokenParts[1];
            this.encodedSignature = tokenParts[2];
        } else {
            this.encodedHeader = "wrong";
            this.encodedPayload = "wrong";
            this.encodedSignature = "wrong";
        }
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

    /**
     * Verifies the token signature.
     * 
     * @return <code>true</code> if token is vaild, else <code>false</code>.
     */
    public boolean isValid() {
        String expectedSignature = hmacSha256(encodedHeader + "." + encodedPayload, secret);
        return expectedSignature.equals(encodedSignature);
    }

    /**
     * Retrieves the user ID from the token.
     * 
     * @return decoded user ID.
     */
    public int getUserId() {
        JSONObject payload = new JSONObject(decode(encodedPayload));
        return payload.getInt("sub");
    }

    /**
     * Retrieves the username from the token.
     * 
     * @return decoded username.
     */
    public String getUsername() {
        JSONObject payload = new JSONObject(decode(encodedPayload));
        return payload.getString("name");
    }

    @Override
    public String toString() {
        return encodedHeader + "." + encodedPayload + "." + encodedSignature;
    }

}
