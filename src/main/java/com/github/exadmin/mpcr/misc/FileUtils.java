package com.github.exadmin.mpcr.misc;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {
    public static boolean isFileAbsent(String path) {
        File file = new File(path);
        return !(file.isFile() && file.exists());
    }

    public static String getFolderOnly(String strPath) {
        Path path = Paths.get(strPath);
        return path.getParent().toString();
    }
}
