package com.util;

import java.io.*;
import java.nio.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.channels.FileChannel;

public class UtilFile {
    public static byte[] readBytes(File filePath) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r");
                FileChannel ch = file.getChannel()) {

            long size = ch.size();
            ByteBuffer buf = ByteBuffer.allocateDirect((int) size); // direct = no heap copy
            while (buf.hasRemaining()) ch.read(buf);
            buf.flip();

            byte[] out = new byte[(int) size];
            buf.get(out);
            return out;
        }
    }

    public static String read(File filePath, Charset charset) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r");
                FileChannel ch = file.getChannel()) {

            long size = ch.size();
            if (size > Integer.MAX_VALUE) throw new IOException("File too large");

            ByteBuffer buf = ByteBuffer.allocateDirect((int) size);
            while (buf.hasRemaining()) ch.read(buf);
            buf.flip();

            // فك التشفير مباشرة
            CharBuffer charBuf = charset.decode(buf);
            return charBuf.toString();
        }
    }

    public static String read(File filePath) throws IOException {
        return read(filePath, StandardCharsets.UTF_8);
    }

    public static String getNameNoExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        int lastDot = filename.lastIndexOf('.'); // find last .
        int lastSlash = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')); // handle
        // paths

        if (lastDot > lastSlash) { // only remove if dot is after last slash
            return filename.substring(lastSlash + 1, lastDot);
        } else {
            return filename.substring(lastSlash + 1); // no extension found
        }
    }

    public static String getNameNoExtension(File file) {
        return getNameNoExtension(file.getName());
    }

    public static String getExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        int lastDot = filename.lastIndexOf('.'); // find last .
        int lastSlash = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')); // handle
        // paths

        if (lastDot > lastSlash) { // only if dot is after last slash and not first char
            return filename.substring(lastDot); // includes the dot
        } else {
            return ""; // no extension
        }
    }

    public static String getExtension(File file) {
        return getExtension(file.getName());
    }

    public static void dir(File dir) {
        if (!dir.exists())
            dir.mkdirs();
    }

    public static void dir(String path) {
        dir(new File(path));
    }

    public static boolean delete(String path) {
        return delete(new File(path));
    }

    public static boolean delete(File dir) {
        if (dir == null || !dir.exists()) return true;

        File[] files = dir.listFiles();
        if (files != null) { // null if not a directory or no permission
            for (File file : files) {
                if (file.isDirectory()) {
                    delete(file); // recursive
                } else {
                    file.delete();
                }
            }
        }
        return dir.delete(); // delete the empty folder
    }
}