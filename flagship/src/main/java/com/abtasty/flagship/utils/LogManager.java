package com.abtasty.flagship.utils;

import java.util.logging.Level;

/**
 * Class to extends in order to provide a custom Log manager.
 */
public abstract class LogManager {

    public enum LogMode {
        NONE, ALL, ERRORS
    }

    protected LogManager.LogMode  mode;

    public LogManager() {
        this.mode = LogMode.ALL;
    }

    public LogManager(LogManager.LogMode mode) {
        if (mode == null)
            this.mode = LogMode.NONE;
        else
            this.mode = mode;
    }

    /**
     * Check if the log should be filtered by Log
     * @param level
     * @return
     */
    protected Boolean isLogApplyToLogMode(Level level) {
        boolean apply = true;
        switch (this.mode) {
            case NONE:
                apply = false;
                break;
            case ALL:
                apply = true;
                break;
            case ERRORS:
                apply = (level == Level.SEVERE || level == Level.WARNING);
                break;
        }
        return apply;
    }

    /**
     * Set the log mode for filtering logs.
     * @param mode
     */
    public void setMode(LogMode mode) {
        this.mode = mode;
    }

    protected void newLog(Level level, String tag, String message) {
        if (isLogApplyToLogMode(level))
            onLog(level, tag, message);
    }

    /**
     * Called when the SDK produce a log.
     * @param level log level.
     * @param tag location where the log come from.
     * @param message log message.
     */
    public abstract void onLog(Level level, String tag, String message);

    /**
     * Called when the SDK produce an Exception.
     * @param e
     */
    public void onException(Exception e) { }
}
