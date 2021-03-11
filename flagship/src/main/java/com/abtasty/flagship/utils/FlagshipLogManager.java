package com.abtasty.flagship.utils;

import com.abtasty.flagship.main.Flagship;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class FlagshipLogManager extends LogManager {

    public enum Tag {
        GLOBAL("[GLOBAL]"),
        INITIALIZATION("[INITIALIZATION]"),
        CONFIGURATION("[CONFIGURATION]"),
        UPDATE_CONTEXT("[UPDATE_CONTEXT]"),
        SYNCHRONIZE("[SYNCHRONIZE]"),
        CAMPAIGNS("[CAMPAIGNS]"),
        PARSING("[PARSING]"),
        GET_MODIFICATION("[GET_MODIFICATION]"),
        GET_MODIFICATION_INFO("[GET_MODIFICATION_INFO]"),
        TRACKING("[HIT]"),
        ACTIVATE("[ACTIVATE]");


        String name = "";

        Tag(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class LogFormatter extends Formatter {

        public static final String RESET = "\033[0m";
        public static final String BLACK = "\033[0;30m";
        public static final String RED = "\033[0;31m";
        public static final String GREEN = "\033[0;32m";
        public static final String YELLOW = "\033[0;33m";
        public static final String BLUE = "\033[0;34m";
        public static final String PURPLE = "\033[0;35m";
        public static final String CYAN = "\033[0;36m";
        public static final String WHITE = "\033[0;2m";

        @Override
        public String format(LogRecord record) {
            StringBuilder builder = new StringBuilder();

            Level level = record.getLevel();
            if (level == Level.INFO) {
                builder.append(WHITE);
            } else if (level == Level.WARNING) {
                builder.append(YELLOW);
            } else if (level == Level.SEVERE) {
                builder.append(RED);
            } else {
                builder.append(WHITE);
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
            builder.append(RESET);
            builder.append("\n");
            return builder.toString();
        }

        private String calcDate(long millisecs) {
            SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date resultDate = new Date(millisecs);
            return date_format.format(resultDate);
        }
    }

    private String          mainTag = "[Flagship]";
    private Logger          logger = Logger.getLogger(FlagshipLogManager.class.getName());

    public FlagshipLogManager(LogManager.LogMode mode) {
        super(mode);
        init();
    }

    public FlagshipLogManager() {
        super();
        init();
    }

    private void init() {
        ConsoleHandler h = new ConsoleHandler();
        Formatter formatter = new LogFormatter();
        h.setFormatter(formatter);
        logger.setUseParentHandlers(false);
        if (logger.getHandlers().length == 0)
            logger.addHandler(h);
    }

    public static void log(Tag tag, Level level, String message) {
        if (Flagship.getConfig() != null) {
            LogManager logManager = Flagship.getConfig().getLogManager();
            if (logManager != null && logManager.isLogApplyToLogMode(level) && tag != null && message != null)
                logManager.newLog(level, tag.getName(), message);
        }
    }

    @Override
    public void onLog(Level level, String tag, String message) {
        logger.log(level, this.mainTag + "[" + level.toString() + "]" + tag + " " + message);
    }
}
