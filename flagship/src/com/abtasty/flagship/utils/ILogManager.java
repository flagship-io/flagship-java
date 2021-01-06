package com.abtasty.flagship.utils;

import com.abtasty.flagship.main.Flagship;

import java.util.logging.Logger;

public interface ILogManager {

    public void onLog(LogManager.Tag tag, LogLevel level, String message);
    public void onException(LogManager.Tag tag, LogLevel level, String stacktrace);
}
