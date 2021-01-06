package com.abtasty.flagship.utils;

import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class FlagshipExceptionHandler implements Thread.UncaughtExceptionHandler {

    private String flagshipPackage = "com.abtasty.flagship";
    private FlagshipConfig config = null;
    private Thread.UncaughtExceptionHandler defaultHandler = null;

    public FlagshipExceptionHandler(FlagshipConfig config, Thread.UncaughtExceptionHandler defaultHandler) {
        this.defaultHandler = defaultHandler;
        this.config = config;
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        StackTraceElement[] elements = e.getStackTrace();
//        if (elements.length > 0) {
//            if (elements[0].getClassName().contains(flagshipPackage)) {
//                StringWriter writer = new StringWriter();
//                PrintWriter printer = new PrintWriter(writer);
//                e.printStackTrace(printer);
//                if (config != null && config.logManager != null)
//                    config.logManager.onException(LogManager.Tag.GLOBAL, LogLevel.EXCEPTION, writer.toString());
//                try {
//                    printer.close();
//                    writer.close();
//                } catch (IOException ioException) {
//                    ioException.printStackTrace();
//                }
//            }
//        }

    }
}
