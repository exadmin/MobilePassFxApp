package com.github.exadmin.mpcr.misc;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;

public class KeyStoreHelperUnsafe {
    private static final String KEY_STORE_TYPE = "PKCS12";
    private static final String KEY_ALGORITHM  = "RSA";
    private static final String KEY_ALIAS      = "mobile-pass-fx-app-key-alias";

    private final String keyStoreFileName;
    private final char[] masterPassword;

    private KeyStore keyStore;
    private final KeyStore.ProtectionParameter keyProtectionPassw;

    public KeyStoreHelperUnsafe(String keyStoreFileName, char[] masterPassword) {
        this.keyStoreFileName = keyStoreFileName;
        this.masterPassword = masterPassword;

        this.keyProtectionPassw = new KeyStore.PasswordProtection(masterPassword);
    }

    public void initializeEmpty() throws Exception {
        keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
        keyStore.load(null, masterPassword);
    }

    public void loadFromDisk() throws Exception {
        keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
        try (InputStream fin = Files.newInputStream(Paths.get(keyStoreFileName))) {
            keyStore.load(fin, masterPassword);
        }
    }

    public void storeToDisk() throws Exception {
        try (FileOutputStream keyStoreOutputStream = new FileOutputStream(keyStoreFileName)) {
            keyStore.store(keyStoreOutputStream, masterPassword);
        }
    }

    public String getUserSecret() throws Exception {
        KeyStore.SecretKeyEntry privateKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, keyProtectionPassw);
        return new String(privateKeyEntry.getSecretKey().getEncoded());
    }

    public void setUserSecret(String userSecretWord) throws Exception {
        SecretKey mySecretKey = new SecretKeySpec(userSecretWord.getBytes(), KEY_ALGORITHM);
        KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(mySecretKey);
        keyStore.setEntry(KEY_ALIAS, secretKeyEntry, keyProtectionPassw);
    }
}
