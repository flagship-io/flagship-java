//package com.abtasty.flagship.utils;
//
//import com.abtasty.flagship.main.FlagshipConfig;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.io.StringWriter;
//
//public class FlagshipExceptionHandler implements Thread.UncaughtExceptionHandler {
//
//    private final static String             flagshipPackage = "com.abtasty.flagship";
//    private Thread.UncaughtExceptionHandler previousHandler = null;
//
//    public FlagshipExceptionHandler() {
//        this.previousHandler = Thread.getDefaultUncaughtExceptionHandler();
//        Thread.setDefaultUncaughtExceptionHandler(this);
//    }
//
//    @Override
//    public void uncaughtException(Thread t, Throwable e) {
//        StackTraceElement[] elements = e.getStackTrace();
//        if (elements.length > 0) {
//            if (elements[0].getClassName().contains(flagshipPackage) && e instanceof Exception)
//                FlagshipLogManager.exception((Exception) e);
//            else if (previousHandler != null)
//                this.previousHandler.uncaughtException(t, e);
//        } else if (previousHandler != null)
//            this.previousHandler.uncaughtException(t, e);
//    }
//}
