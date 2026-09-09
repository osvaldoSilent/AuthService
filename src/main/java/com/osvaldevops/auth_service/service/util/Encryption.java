package com.osvaldevops.auth_service.service.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class Encryption {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;  // 12 bytes es el estándar para GCM
    private static final int GCM_TAG_LENGTH = 128; // Autenticación de 128 bits

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public Encryption(@Value("${app.security.encryption-secret-key}") String secretKeyBase64) {
        byte[] decodedKey = Base64.getDecoder().decode(secretKeyBase64);
        if (decodedKey.length != 32) {
            throw new IllegalArgumentException("La clave secreta debe ser de 256 bits (32 bytes).");
        }
        
        this.secretKey = new SecretKeySpec(decodedKey, "AES");
    }

    public String encrypt(String plainText) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Concatenación optimizada usando ByteBuffer
            byte[] finalPayload = ByteBuffer.allocate(iv.length + cipherText.length)
                    .put(iv)
                    .put(cipherText)
                    .array();

            return Base64.getEncoder().encodeToString(finalPayload);
        } catch (Exception e) {
            throw new IllegalStateException("Error al encriptar payload con AES-GCM", e);
        }
    }

    /**
     * Lee la cabecera del IV y descifra el texto.
     */
    public String decrypt(String encryptedTextBase64) {
        try {
            byte[] decodedMessage = Base64.getDecoder().decode(encryptedTextBase64);

            ByteBuffer byteBuffer = ByteBuffer.wrap(decodedMessage);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv); // Extrae los primeros 12 bytes (IV)

            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Error al desencriptar payload con AES-GCM", e);
        }
    }
}