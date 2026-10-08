package com.cybersentinel.service;

import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class ApiCredentialService {

    private final SecureRandom secureRandom = new SecureRandom();

    public ApiCredential generateCredential() {
        byte[] secretBytes = new byte[32];
        secureRandom.nextBytes(secretBytes);
        
        byte[] keyBytes = new byte[16];
        secureRandom.nextBytes(keyBytes);

        String rawSecret = "csk_secret_" + Base64.getUrlEncoder().withoutPadding().encodeToString(secretBytes);
        String apiKey = "csk_live_" + Base64.getUrlEncoder().withoutPadding().encodeToString(keyBytes);

        String secretHash = hashSecret(rawSecret);
        String secretMasked = "csk_secret_•••••" + rawSecret.substring(rawSecret.length() - 4);

        return new ApiCredential(apiKey, rawSecret, secretHash, secretMasked);
    }

    public String hashSecret(String rawSecret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawSecret.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash secret", e);
        }
    }

    public boolean verifySecret(String rawSecret, String storedHash) {
        String inputHash = hashSecret(rawSecret);
        return inputHash.equals(storedHash);
    }

    public static class ApiCredential {
        private final String apiKey;
        private final String rawSecret;
        private final String secretHash;
        private final String secretMasked;

        public ApiCredential(String apiKey, String rawSecret, String secretHash, String secretMasked) {
            this.apiKey = apiKey;
            this.rawSecret = rawSecret;
            this.secretHash = secretHash;
            this.secretMasked = secretMasked;
        }

        public String getApiKey() {
            return apiKey;
        }

        public String getRawSecret() {
            return rawSecret;
        }

        public String getSecretHash() {
            return secretHash;
        }

        public String getSecretMasked() {
            return secretMasked;
        }
    }
}
