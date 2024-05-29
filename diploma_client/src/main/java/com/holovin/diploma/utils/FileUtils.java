package com.holovin.diploma.utils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static byte[] convertUsingFileInputStream(String pathDir) throws IOException {
        List<byte[]> fileBytesList = new ArrayList<>();

        File directory = new File(pathDir);
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        fileBytesList.add(readFileToBytes(file));
                    }
                }
            }
        }

        return concatenateBytes(fileBytesList);
    }

    public static byte[] readFileToBytes(File file) throws IOException {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            fileInputStream.read(bytes);
            return bytes;
        } finally {
            IOUtils.closeQuietly(fileInputStream);
        }
    }

    public static byte[] concatenateBytes(List<byte[]> byteArrays) {
        int totalLength = byteArrays.stream().mapToInt(byteArray -> byteArray.length).sum();
        byte[] result = new byte[totalLength];

        int currentIndex = 0;
        for (byte[] byteArray : byteArrays) {
            System.arraycopy(byteArray, 0, result, currentIndex, byteArray.length);
            currentIndex += byteArray.length;
        }

        return result;
    }
}
