package com.springboot.service;

import com.abtasty.flagship.utils.LogManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogHelper {

    private static final String filename = "logs";

    public static void clearLogFile() {
        File fd = new File(filename);
        if (fd.delete()) {
            System.out.println("Deleted file: " + filename);
        } else {
            System.out.println("Failed to delete log file.");
        }
    }

    private static String currentDate() {
        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date resultDate = new Date(System.currentTimeMillis());
        return date_format.format(resultDate);
    }

    public static void appendToLogFile(LogManager.Level level, String tag, String message) {
        try {
            String log = String.format("[%s][%s][%s][%s] %s \n",
                    currentDate(),
                    "Flagship",
                    level.toString(),
                    tag,
                    message);
            Boolean exists = new File(filename).createNewFile();
            Files.write(Paths.get(filename), log.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getLogFileContent() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
            reader.close();
        } catch (Exception e) {
            System.out.println("Error happened while reading log file.");
        }
        return stringBuilder.toString();
    }
}
