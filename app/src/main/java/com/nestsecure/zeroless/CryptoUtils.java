package com.nestsecure.zeroless;

import android.content.Context;
import android.util.Base64;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_SIZE = 12; // Recommended for GCM
    private static final int TAG_SIZE = 16; // 128 bits

    public static String encrypt(String plaintext, String key) throws Exception {
        byte[] iv = new byte[IV_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_SIZE * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

        byte[] encrypted = cipher.doFinal(plaintext.getBytes());
        byte[] result = new byte[IV_SIZE + encrypted.length];
        System.arraycopy(iv, 0, result, 0, IV_SIZE);
        System.arraycopy(encrypted, 0, result, IV_SIZE, encrypted.length);

        return Base64.encodeToString(result, Base64.DEFAULT);
    }

    public static String decrypt(String ciphertext, String key) throws Exception {
        byte[] decoded = Base64.decode(ciphertext, Base64.DEFAULT);
        if (decoded.length < IV_SIZE) {
            throw new IllegalArgumentException("Invalid ciphertext");
        }

        byte[] iv = new byte[IV_SIZE];
        System.arraycopy(decoded, 0, iv, 0, IV_SIZE);
        byte[] encrypted = new byte[decoded.length - IV_SIZE];
        System.arraycopy(decoded, IV_SIZE, encrypted, 0, encrypted.length);

        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_SIZE * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted);
    }

    public static void saveEncryptedKey(Context context, String encryptedKey, boolean useSdCard) throws IOException {
        String filePath = useSdCard ? context.getExternalFilesDir(null) + "/key.txt" : context.getFilesDir() + "/key.txt";
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(encryptedKey.getBytes());
        }
    }

    public static String readEncryptedKey(Context context, boolean useSdCard) throws IOException {
        String filePath = useSdCard ? context.getExternalFilesDir(null) + "/key.txt" : context.getFilesDir() + "/key.txt";
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] data = new byte[fis.available()];
            fis.read(data);
            return new String(data);
        }
    }

    public static String encryptKey(String commonKey, String personalKey) throws Exception {
        return encrypt(commonKey, personalKey);
    }

    public static String decryptKey(String encryptedKey, String personalKey) throws Exception {
        return decrypt(encryptedKey, personalKey);
    }
}
