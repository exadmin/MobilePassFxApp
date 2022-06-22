package com.github.exadmin.mpcr.misc;

import javafx.scene.control.Alert;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Properties;

public class Settings {
    public static final int DELAY_BETWEEN_FRAMES_MS = 41;
    public static final long TIME_TO_WAIT_BEFORE_START_VPN_CONNECTING_SECONDS = 5;

    public static final String FX_STYLE_LABEL_UNFOCUSED = "font-weight: regular; -fx-text-fill: LIGHTGRAY;";
    public static final String FX_STYLE_LABEL_FOCUSED   = "font-weight: bold; -fx-text-fill: YELLOW;";
    public static final String FX_STYLE_PASSWORD_PROMPT_STYLE_VALUE_IS_SET = "-fx-prompt-text-fill: green;";
    public static final String FX_STYLE_PASSWORD_PROMPT_STYLE_NEED_INPUT = "-fx-prompt-text-fill: gray;";
    public static final String UI_BIG_CAPTION_DEFAULT_TEXT = "Waiting for digits recognition...";


    private static final String PROP_NAME_VPNCLI_PATH = "vpncli-file-full-path";
    private static final String PROP_NAME_VPN_HOST = "vpn-host";
    private static final String PROP_NAME_KEYSTORE_PATH = "keystore-file-full-path";
    private static final String PROP_NAME_KEYSTORE_PASSWORD = "keystore-master-password";
    private static final String PROP_NAME_NT_LOGIN = "nt-login";
    private static final String PROP_NAME_AUTO_STOP = "auto-stop-vpn-agents";

    private static final Properties properties = new Properties();

    private static String ntPassword = null;

    public static Exception loadFromFile() {
        try {
            properties.load(Files.newInputStream(Paths.get("./settings.dat")));

            KeyStoreHelperUnsafe keyStoreHelper = new KeyStoreHelperUnsafe(getKeyStorePath(), getKeyStoreMasterPassword());
            keyStoreHelper.loadFromDisk();
            setNtPassword(keyStoreHelper.getUserSecret());

            return null;
        } catch (NoSuchFileException nsfe) {
            return new Exception("Settings file 'settings.dat' is not found in the working dir");
        } catch (Exception ex) {
            ex.printStackTrace();
            return ex;
        }
    }

    public static Exception saveSettingsToFile() {
        try {
            properties.setProperty(PROP_NAME_VPNCLI_PATH, getVpncliPath());
            properties.setProperty(PROP_NAME_VPN_HOST, getVpnHost());
            properties.setProperty(PROP_NAME_KEYSTORE_PATH, getKeyStorePath());
            properties.setProperty(PROP_NAME_KEYSTORE_PASSWORD, getKeyStoreMasterPasswordAsString());
            properties.setProperty(PROP_NAME_AUTO_STOP, isAutoStopEnabled().toString());

            properties.store(Files.newOutputStream(Paths.get("./settings.dat")), "MobilePassFxApp");

            // check if keystore file is set
            if (getKeyStorePath() == null && StrUtils.isStringEmpty(getKeyStorePath(), true)) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setHeaderText("NT Password will not be saved - as no keystore file is specified");
                alert.show();
                return null;
            }

            if (StrUtils.isStringEmpty(getNtPassword(), false)) {
                return new Exception("No NT Password is specified. Nothing to update in keystore.");
            }

            if (StrUtils.isStringEmpty(getNtLogin(), true)) {
                return new Exception("No 'NT Login' is specified. Please set and save settings once again.");
            }


            KeyStoreHelperUnsafe keyStoreHelper = new KeyStoreHelperUnsafe(getKeyStorePath(), getKeyStoreMasterPassword());
            if (FileUtils.isFileExist(getKeyStorePath())) {
                keyStoreHelper.loadFromDisk();
            } else {
                keyStoreHelper.initializeEmpty();
            }

            keyStoreHelper.setUserSecret(getNtPassword());
            keyStoreHelper.storeToDisk();

            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return ex;
        }
    }

    public static String getVpncliPath() {
        return properties.getProperty(PROP_NAME_VPNCLI_PATH, "c:\\Program Files (x86)\\Cisco\\Cisco AnyConnect Secure Mobility Client\\vpncli.exe");
    }

    public static String getVpnHost() {
        return properties.getProperty(PROP_NAME_VPN_HOST, "myvpn.example.com");
    }

    public static String getKeyStorePath() {
        return properties.getProperty(PROP_NAME_KEYSTORE_PATH, "./mykeystore.ks");
    }

    public static char[] getKeyStoreMasterPassword() {
        return getKeyStoreMasterPasswordAsString().toCharArray();
    }

    public static String getKeyStoreMasterPasswordAsString() {
        if (StrUtils.isStringEmpty(properties.getProperty(PROP_NAME_KEYSTORE_PASSWORD), false)) {
            return "";
        } else {
            return properties.getProperty(PROP_NAME_KEYSTORE_PASSWORD);
        }
    }

    public static Boolean isAutoStopEnabled() {
        return Boolean.parseBoolean(properties.getProperty(PROP_NAME_AUTO_STOP, "false"));
    }

    public static String getNtLogin() {
        return properties.getProperty(PROP_NAME_NT_LOGIN, "");
    }

    public static String getNtPassword() {
        return ntPassword;
    }

    public static void setVpnCliPath(String value) {
        properties.setProperty(PROP_NAME_VPNCLI_PATH, value);
    }

    public static void setVpnHost(String value) {
        properties.setProperty(PROP_NAME_VPN_HOST, value);
    }

    public static void setKeyStorePath(String value) {
        properties.setProperty(PROP_NAME_KEYSTORE_PATH, value);
    }

    public static void setKeyStoreMasterPassword(String value) {
        properties.setProperty(PROP_NAME_KEYSTORE_PASSWORD, value);
    }

    public static void setAutoStopEnabled(String value) {
        properties.setProperty(PROP_NAME_AUTO_STOP, value);
    }

    public static void setNTLogin(String value) {
        properties.setProperty(PROP_NAME_NT_LOGIN, value);
    }

    public static void setNtPassword(String ntPasswordArray) {
        ntPassword = ntPasswordArray;
    }

    public static String checkSettingsOrReturnErrorDescription() {
        if (!FileUtils.isFileExist("./settings.dat")) {
            return "Not settings file is found. Seems you've started application for the first time";
        }

        if (!FileUtils.isFileExist(getVpncliPath())) {
            return "Path to vpn-agent is not specified or file does not exist";
        }

        if (!FileUtils.isFileExist(getKeyStorePath())) {
            return "Path to java-key-store is not specified or file does not exist";
        }

        if (StrUtils.isStringEmpty(getVpnHost(), true)) {
            return "VPN host address is not specified";
        }

        if (StrUtils.isStringEmpty(getKeyStoreMasterPasswordAsString(), false)) {
            return "KeyStore Master password is not specified";
        }

        // try load keystore using master-key
        try {
            KeyStoreHelperUnsafe keyStoreHelper = new KeyStoreHelperUnsafe(getKeyStorePath(), getKeyStoreMasterPassword());
            keyStoreHelper.loadFromDisk();

            String secretKey = keyStoreHelper.getUserSecret();
            if (StrUtils.isStringEmpty(secretKey, false)) {
                return "There is no 1st factor (NT password) stored in keystore";
            }
        } catch (FileNotFoundException nfne) {
            return "Can't find keystore file at " + getKeyStorePath();
        } catch (IOException ioe) {
            return "Can't read keystore file at " + getKeyStorePath();
        } catch (NoSuchAlgorithmException nsae) {
            return "Can't read keystore as no supported algorithm found. Details = " + nsae;
        } catch (CertificateException ce) {
            return "Can't read keystore, certificate exception happened. Details = " + ce;
        } catch (KeyStoreException kse) {
            return "Can't read keystore. Details = " + kse;
        } catch (UnrecoverableEntryException uee) {
            return "Can't extract secret from keystore. Please check minimal version of JDK is 1.8.0_202 is used. Details = " + uee;
        } catch (Exception ex) {
            return "Undefined exception occured. Details = " + ex;
        }

        if (StrUtils.isStringEmpty(getNtLogin(), true)) {
            return "NT login is not specified";
        }

        return null;
    }
}
