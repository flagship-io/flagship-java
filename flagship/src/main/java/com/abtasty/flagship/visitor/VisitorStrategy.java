package com.abtasty.flagship.visitor;

import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;

import java.util.HashMap;

abstract class VisitorStrategy implements IVisitor {

    protected VisitorDelegate visitorDelegate;

    public VisitorStrategy(VisitorDelegate visitorDelegate) {
        this.visitorDelegate = visitorDelegate;
    }

    protected void logMethodDeactivatedError(FlagshipLogManager.Tag tag, String methodName) {
        FlagshipLogManager.log(tag, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.METHOD_DEACTIVATED_ERROR, methodName, Flagship.getStatus()));
    }

    abstract void sendContextRequest();

    abstract void sendConsentRequest();

    abstract void loadContext(HashMap<String, Object> context);
}