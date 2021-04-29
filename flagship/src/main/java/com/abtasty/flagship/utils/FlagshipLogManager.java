package com.abtasty.flagship.utils;

import com.abtasty.flagship.main.Flagship;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class FlagshipLogManager extends LogManager {

    public enum Tag {
        GLOBAL("GLOBAL"),
        INITIALIZATION("INITIALIZATION"),
        CONFIGURATION("CONFIGURATION"),
        BUCKETING("BUCKETING"),
        UPDATE_CONTEXT("UPDATE_CONTEXT"),
        SYNCHRONIZE("SYNCHRONIZE"),
        CAMPAIGNS("CAMPAIGNS"),
        PARSING("PARSING"),
        TARGETING("TARGETING"),
        ALLOCATION("ALLOCATION"),
        GET_MODIFICATION("GET_MODIFICATION"),
        GET_MODIFICATION_INFO("GET_MODIFICATION_INFO"),
        TRACKING("HIT"),
        ACTIVATE("ACTIVATE"),
        EXCEPTION("EXCEPTION");

        String name = "";

        Tag(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class LogFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            return String.format("%s\n", record.getMessage());
        }
    }

    private final String            mainTag = "Flagship";
    private final Logger            logger = Logger.getLogger(Flagship.class.getName());

    public static final String      RESET = "\u001B[0m";
    public static final String      RED = "\u001B[31m";
    public static final String      GREEN = "\u001B[32m";
    public static final String      YELLOW = "\u001B[33m";
    public static final String      BLUE = "\u001B[34m";
    public static final String      PURPLE = "\u001B[35m";
    public static final String      CYAN = "\u001B[36m";
    public static final String      WHITE = "\u001B[97m";

    public FlagshipLogManager(LogManager.Level mode) {
        super(mode);
        init();
    }

    public FlagshipLogManager() {
        super();
        init();
    }

    private void init() {
        ConsoleHandler h = new ConsoleHandler();
        h.setLevel(java.util.logging.Level.ALL);
        Formatter formatter = new LogFormatter();
        h.setFormatter(formatter);
        logger.setUseParentHandlers(false);
        if (logger.getHandlers().length == 0)
            logger.addHandler(h);
    }

    public static void log(Tag tag, Level level, String message) {
        if (Flagship.getConfig() != null) {
            LogManager logManager = Flagship.getConfig().getLogManager();
            if (logManager != null && tag != null && message != null)
                logManager.newLog(level, tag.getName(), message);
        }
    }

    public static void exception(Exception e) {
        if (Flagship.getConfig() != null) {
            LogManager logManager = Flagship.getConfig().getLogManager();
            logManager.onException(e);
        }
    }

    private String exceptionToString(Exception e) {
        String strException = null;
        try {
            StringWriter writer = new StringWriter();
            PrintWriter printer = new PrintWriter(writer);
            e.printStackTrace(printer);
            strException = writer.toString();
            printer.close();
            writer.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return strException;
    }

    private String currentDate() {
        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date resultDate = new Date(System.currentTimeMillis());
        return date_format.format(resultDate);
    }

    private String getColor(Level level) {
        String color;
        switch (level) {
            case EXCEPTIONS:
            case ERROR:
                color = RED;
                break;
            case WARNING:
                color = YELLOW;
                break;
            case DEBUG:
                color = CYAN;
                break;
            case INFO:
                color = GREEN;
                break;
            default:
                color = WHITE;
                break;
        }
        return color;
    }

    @Override
    public void onLog(Level level, String tag, String message) {
        String log = String.format("%s[%s][%s][%s][%s] %s %s",
                getColor(level),
                currentDate(),
                this.mainTag,
                level.toString(),
                tag,
                message,
                RESET);
        logger.log(java.util.logging.Level.INFO, log);
    }

    @Override
    public void onException(Exception e) {
        String strException = exceptionToString(e);
        onLog(Level.EXCEPTIONS, Tag.EXCEPTION.getName(), strException);
    }
}
