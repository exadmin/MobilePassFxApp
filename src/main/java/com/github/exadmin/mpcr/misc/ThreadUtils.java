package com.github.exadmin.mpcr.misc;

public class ThreadUtils {
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            // ok to suppress exception here
        }
    }
}
