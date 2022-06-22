package com.github.exadmin.mpcr.misc;

public class StrUtils {
    public static boolean isStringEmpty(String string, boolean allowTrim) {
        if (string == null) return true;

        if (allowTrim) {
            string = string.trim();
        }

        return string.length() == 0;
    }

    public static boolean isStringNonEmpty(String string, boolean allowTrim) {
        return !isStringEmpty(string, allowTrim);
    }
}
