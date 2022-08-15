package com.github.exadmin.mpcr.misc;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class Settings {
    private static final String SETTINGS_FILE_NAME = "settings.properties";

    public static final int DELAY_BETWEEN_FRAMES_MS = 41;
    public static final long TIME_TO_WAIT_BEFORE_START_VPN_CONNECTING_SECONDS = 5;

    public static final String FX_STYLE_LABEL_UNFOCUSED = "font-weight: regular; -fx-text-fill: LIGHTGRAY;";
    public static final String FX_STYLE_LABEL_FOCUSED   = "font-weight: bold; -fx-text-fill: YELLOW;";
    public static final String FX_STYLE_PASSWORD_PROMPT_STYLE_VALUE_IS_SET = "-fx-prompt-text-fill: green;";
    public static final String FX_STYLE_PASSWORD_PROMPT_STYLE_NEED_INPUT = "-fx-prompt-text-fill: gray;";
    public static final String UI_BIG_CAPTION_DEFAULT_TEXT = "Waiting for digits recognition...";
    public static final String APPLICATION_CAPTION = "Mobile Pass Code Recognizer, version 1.2";

    private static final String PROP_NAME_VPNCLI_PATH = "vpncli-file-full-path";
    private static final String PROP_NAME_VPN_HOST = "vpn-host";
    private static final String PROP_NAME_NT_LOGIN = "nt-login";
    private static final String PROP_NAME_NT_PASSWORD_ENCODED_VALUE = "nt-password";
    private static final String PROP_NAME_AUTO_STOP = "auto-stop-vpn-agents";

    private static final Properties properties = new Properties();

    public static void loadFromFile() {
        try {
            properties.load(Files.newInputStream(Paths.get("./" + SETTINGS_FILE_NAME)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Exception saveSettingsToFile() {
        try {
            properties.setProperty(PROP_NAME_VPNCLI_PATH, getVpncliPath());
            properties.setProperty(PROP_NAME_VPN_HOST, getVpnHost());
            properties.setProperty(PROP_NAME_AUTO_STOP, isAutoStopEnabled().toString());

            properties.store(Files.newOutputStream(Paths.get("./" + SETTINGS_FILE_NAME)), "MobilePassFxApp");

            if (!checkNtPasswordIsSet()) {
                return new Exception("No NT Password is specified. Nothing to update in keystore.");
            }

            if (StrUtils.isStringEmpty(getNtLogin(), true)) {
                return new Exception("No 'NT Login' is specified. Please set and save settings once again.");
            }

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

    public static Boolean isAutoStopEnabled() {
        return Boolean.parseBoolean(properties.getProperty(PROP_NAME_AUTO_STOP, "false"));
    }

    public static String getNtLogin() {
        return properties.getProperty(PROP_NAME_NT_LOGIN, "");
    }

    public static void setVpnCliPath(String value) {
        properties.setProperty(PROP_NAME_VPNCLI_PATH, value);
    }

    public static void setVpnHost(String value) {
        properties.setProperty(PROP_NAME_VPN_HOST, value);
    }

    public static void setAutoStopEnabled(String value) {
        properties.setProperty(PROP_NAME_AUTO_STOP, value);
    }

    public static void setNTLogin(String value) {
        properties.setProperty(PROP_NAME_NT_LOGIN, value);
    }

    public static String checkSettingsOrReturnErrorDescription() {
        if (FileUtils.isFileAbsent("./" + SETTINGS_FILE_NAME)) {
            return "No settings file is found. Seems you've started application for the first time";
        }

        if (FileUtils.isFileAbsent(getVpncliPath())) {
            return "Path to vpn-agent is not specified or file does not exist";
        }

        if (StrUtils.isStringEmpty(getVpnHost(), true)) {
            return "VPN host address is not specified";
        }

        if (StrUtils.isStringEmpty(getNtLogin(), true)) {
            return "NT login is not specified";
        }

        if (!checkNtPasswordIsSet()) {
            return "No password is set for the NT login";
        }

        return null;
    }

    public static String getNtPassword(String passPhrase) {
        return MyEncryptor.decrypt(properties.getProperty(PROP_NAME_NT_PASSWORD_ENCODED_VALUE), passPhrase);
    }

    public static void setNtPassword(String password, String passPhrase) {
        properties.setProperty(PROP_NAME_NT_PASSWORD_ENCODED_VALUE, MyEncryptor.encrypt(password, passPhrase));
    }

    public static boolean checkNtPasswordIsSet() {
        return StrUtils.isStringNonEmpty(properties.getProperty(PROP_NAME_NT_PASSWORD_ENCODED_VALUE), true);
    }
}
