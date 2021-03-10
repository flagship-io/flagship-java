package com.abtasty.flagship.utils;

import java.util.logging.Level;

/**
 * Class to extends in order to provide a custom Log manager.
 */
public abstract class ILogManager {

    public enum LogMode {
        NONE, ALL, ERRORS
    }

    protected ILogManager.LogMode  mode;

    public ILogManager() {
        this.mode = LogMode.ALL;
    }

    public ILogManager(ILogManager.LogMode mode) {
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

    public void setMode(LogMode mode) {
        this.mode = mode;
    }

    public abstract void onLog(Level level, String tag, String message);
}
