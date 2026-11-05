package com.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;

public class Utils {

    public final static int SAFTY_MODE_THROW = 0;
    public final static int SAFTY_MODE_MAKE = 1;

    public static File getWorkspace() {
        return getWorkSpace(Utils.class);
    }

    private static File getWorkSpace(Class<?> clazz) {
        try {
            CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
            if (codeSource == null || codeSource.getLocation() == null) {
                // Running from IDE / no jar - fallback to user.dir
                return new File(System.getProperty("user.dir")).getAbsoluteFile();
            }
            URL location = codeSource.getLocation();
            File jarFile = new File(location.toURI());
            // If it's a jar, parent is workspace. If it's a classes dir, use it.
            if (jarFile.isFile()) {
                return jarFile.getParentFile();
            } else {
                return jarFile;
            }
        } catch (URISyntaxException | IllegalArgumentException e) {
            throw new RuntimeException("Cannot resolve workspace", e);
        }
    }

    public final static String format(String text, Object... values) {
        return String.format(text, values);
    }

    public static void safty(String path, boolean isFile) {
        safty(path, isFile, SAFTY_MODE_THROW);
    }

    public static void safty(File file, boolean isFile) {
        safty(file, isFile, SAFTY_MODE_THROW);
    }

    public static void safty(String path, boolean isFile, int saftyMode) {
        safty(new File(path), isFile, saftyMode);
    }

    public static void safty(File file, boolean isFile, int saftyMode) {
        if (file == null) {
            throw new NullPointerException("file is null");
        }

        // Normalize to absolute to avoid null parent problem
        File absFile = file.getAbsoluteFile();

        // 1. If exists -> validate type only
        if (absFile.exists()) {
            if (isFile && !absFile.isFile()) {
                throw new RuntimeException("Must be file: " + absFile.getAbsolutePath());
            }
            if (!isFile && !absFile.isDirectory()) {
                throw new RuntimeException("Must be dir: " + absFile.getAbsolutePath());
            }
            return;
        }

        // 2. Not exists
        if (saftyMode == SAFTY_MODE_THROW) {
            throw new RuntimeException("Not exists: " + absFile.getAbsolutePath());
        }

        if (saftyMode != SAFTY_MODE_MAKE) {
            throw new IllegalArgumentException("Unknown saftyMode: " + saftyMode);
        }

        // 3. MAKE mode
        if (isFile) {
            File parent = absFile.getParentFile();
            if (parent != null && !parent.exists()) {
                if (!parent.mkdirs() && !parent.exists()) {
                    throw new RuntimeException("Cannot create parent dir: " + parent.getAbsolutePath());
                }
            }
            // Parent exists but is a file -> cannot create file inside it
            if (parent != null && parent.exists() && !parent.isDirectory()) {
                throw new RuntimeException("Parent is not a directory: " + parent.getAbsolutePath());
            }
            try {
                if (!absFile.createNewFile() && !absFile.exists()) {
                    throw new RuntimeException("Cannot create file: " + absFile.getAbsolutePath());
                }
            } catch (IOException e) {
                throw new RuntimeException("Cannot create file: " + absFile.getAbsolutePath() + " : " + e.getMessage(), e);
            }
        } else {
            // Directory
            if (!absFile.mkdirs() && !absFile.exists()) {
                throw new RuntimeException("Cannot create dir: " + absFile.getAbsolutePath());
            }
        }
    }
}