package com.github.exadmin.mpcr.examples;

import com.github.exadmin.mpcr.misc.KeyStoreHelperUnsafe;

public class KeyStoreExampleApp {
    public static final String KEYSTORE_FILE_NAME = "./test.ks";
    private static final char[] MASTER_PASSWORD = "changeit".toCharArray();

    public static void main(String[] args) throws Exception {

        KeyStoreHelperUnsafe keyStoreHelper = new KeyStoreHelperUnsafe(KEYSTORE_FILE_NAME, MASTER_PASSWORD);

        // create new one
        if (false) {
            keyStoreHelper.initializeEmpty();
            keyStoreHelper.setUserSecret("very-secret");
            keyStoreHelper.storeToDisk();
        }

        // load secret word from disk
        keyStoreHelper.loadFromDisk();
        String secretWord = keyStoreHelper.getUserSecret();
        System.out.println("Secret word = " + secretWord);

        // override secret word
        keyStoreHelper.setUserSecret("new-secret");
        keyStoreHelper.storeToDisk();
    }
}
