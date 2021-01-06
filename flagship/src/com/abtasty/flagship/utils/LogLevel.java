package com.abtasty.flagship.utils;

import java.util.logging.Level;

public enum LogLevel {
    INFO(Level.INFO),
    FINE(Level.FINE),
    WARNING(Level.WARNING),
    ERROR(Level.SEVERE),
    EXCEPTION(Level.SEVERE);


    private Level value = Level.ALL;

    LogLevel(Level level) {
        this.value = level;
    }

    public Level getValue() {
        return value;
    }
}