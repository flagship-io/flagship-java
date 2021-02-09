package com.abtasty.flagship.utils;

import com.abtasty.flagship.main.Flagship;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class LogManager implements ILogManager {

    public enum Tag {
        GLOBAL("[GLOBAL]"),
        INITIALIZATION("[INITIALIZATION]"),
        CONFIGURATION("[CONFIGURATION]"),
        UPDATE_CONTEXT("[UPDATE_CONTEXT]"),
        SYNCHRONIZE("[SYNCHRONIZE]"),
        CAMPAINGS("[CAMPAIGNS]");

        String name = "";

        Tag(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public class LogFormatter extends Formatter {

        public static final String ANSI_RESET = "\u001B[0m";
        public static final String ANSI_BLACK = "\u001B[30m";
        public static final String ANSI_RED = "\u001B[31m";
        public static final String ANSI_GREEN = "\u001B[32m";
        public static final String ANSI_YELLOW = "\u001B[33m";
        public static final String ANSI_BLUE = "\u001B[34m";
        public static final String ANSI_PURPLE = "\u001B[35m";
        public static final String ANSI_CYAN = "\u001B[36m";
        public static final String ANSI_WHITE = "\u001B[37m";

        @Override
        public String format(LogRecord record) {
            StringBuilder builder = new StringBuilder();

            Level level = record.getLevel();
            if(level == Level.INFO) {
                builder.append(ANSI_GREEN);
            } else if(level == Level.WARNING) {
                builder.append(ANSI_YELLOW);
            } else if(level == Level.SEVERE) {
                builder.append(ANSI_RED);
            } else {
                builder.append(ANSI_WHITE);
            }

            builder.append("[");
            builder.append(calcDate(record.getMillis()));
            builder.append("]");

            builder.append(record.getMessage());

            Object[] params = record.getParameters();

            if (params != null)
            {
                builder.append("\t");
                for (int i = 0; i < params.length; i++)
                {
                    builder.append(params[i]);
                    if (i < params.length - 1)
                        builder.append(", ");
                }
            }

            builder.append(ANSI_RESET);
            builder.append("\n");
            return builder.toString();
        }

        private String calcDate(long millisecs) {
            SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date resultdate = new Date(millisecs);
            return date_format.format(resultdate);
        }
    }

    private Flagship.Log logMode = Flagship.Log.ALL;
    private String mainTag = "[Flagship]";
    private Logger logger = Logger.getLogger(LogManager.class.getName());

    public LogManager(Flagship.Log logMode) {
        if (logMode != null)
            this.logMode = logMode;
        ConsoleHandler h = new ConsoleHandler();
        Formatter formatter = new LogFormatter();
        h.setFormatter(formatter);
        logger.setUseParentHandlers(false);
        if (logger.getHandlers().length == 0)
            logger.addHandler(h);
    }

    @Override
    public void onLog(Tag tag, LogLevel level, String message) {
        if (checkLogModeAllowed(level) && tag != null && message != null) {
            logger.log(level.getValue(), this.mainTag + "[" + level.toString() + "]" + tag.getName() + " " + message);
        }
    }

    @Override
    public void onException(Tag tag, LogLevel level, String stacktrace) {
        onLog(tag, level, stacktrace);
    }

    private Boolean checkLogModeAllowed(LogLevel level) {
        boolean check = true;
        switch (this.logMode) {
            case NONE:
                check = false;
                break;
            case ALL:
                check = true;
                break;
            case ERRORS:
                check =  (level == LogLevel.ERROR || level == LogLevel.WARNING || level == LogLevel.EXCEPTION);
                break;
        }
        return check;
    }
}
