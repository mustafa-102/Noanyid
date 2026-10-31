package com.util;

import java.io.*;
import java.net.*;
import java.security.*;

import com.*;

public class Utils {

    public final static int SAFTY_MODE_THROW = 0;
    public final static int SAFTY_MODE_MAKE = 1;

    public static File getWorkspace() {
        return getWorkSpace(Main.class);
    }

    private static File getWorkSpace(Class<?> clazz) {
        try {
            CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
            File jarFile = new File(codeSource.getLocation().toURI());
            return jarFile.getParentFile();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
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
            throw new NullPointerException();
        }
        if (isFile && file.isDirectory()) {
            throw new RuntimeException("Must be file: " + file);
        }
        if (!isFile && file.isFile()) {
            throw new RuntimeException("Must be dir : " + file);
        }
        if (!file.exists()) {
            switch (saftyMode) {
                case SAFTY_MODE_MAKE:
                    if(isFile)
                    {
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            throw new RuntimeException("Write permission denited.");
                        }
                    }
                    else
                    {
                        file.mkdirs();
                    }
                    break;
                case SAFTY_MODE_THROW:
                    throw new RuntimeException();
            }
        }
    }
}