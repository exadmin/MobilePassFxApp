package com.github.exadmin.mpcr.misc;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public class MyEncryptor {

    public static String encrypt(String sourceText, String passPhrase) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            KeySpec spec = new PBEKeySpec(passPhrase.toCharArray(), salt, 65536, 256); // AES-256
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] key = f.generateSecret(spec).getEncoded();
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

            byte[] ivBytes = new byte[16];
            random.nextBytes(ivBytes);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);

            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, keySpec, iv);
            byte[] encValue = c.doFinal(sourceText.getBytes());

            byte[] finalCiphertext = new byte[encValue.length + 2 * 16];
            System.arraycopy(ivBytes, 0, finalCiphertext, 0, 16);
            System.arraycopy(salt, 0, finalCiphertext, 16, 16);
            System.arraycopy(encValue, 0, finalCiphertext, 32, encValue.length);

            byte[] base64Encoded = Base64.getEncoder().encode(finalCiphertext);
            return new String(base64Encoded);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static String decrypt(String encryptedStringInBase64Format, String passPhrase) {
        try {
            byte[] finalCiphertext = Base64.getDecoder().decode(encryptedStringInBase64Format.getBytes());

            byte[] ivBytes = new byte[16];
            byte[] salt = new byte[16];
            byte[] encValue = new byte[finalCiphertext.length - 32];

            System.arraycopy(finalCiphertext, 0, ivBytes, 0, 16);
            System.arraycopy(finalCiphertext, 16, salt, 0, 16);
            System.arraycopy(finalCiphertext, 32, encValue, 0, finalCiphertext.length - 32);

            KeySpec spec = new PBEKeySpec(passPhrase.toCharArray(), salt, 65536, 256); // AES-256
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] key = f.generateSecret(spec).getEncoded();
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

            IvParameterSpec iv = new IvParameterSpec(ivBytes);

            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE, keySpec, iv);
            byte[] decValue = c.doFinal(encValue);

            return new String(decValue);
        } catch (BadPaddingException | InvalidKeyException ex) {
            // Given final block not properly padded. Such issues can arise if a bad key is used during decryption.
            // In case QR code is recognized incorrectly - we do not want redundant stacktraces in the log to be printed
            // So let's suppress exception
            return null;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}