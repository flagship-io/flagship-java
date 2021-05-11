package com.abtasty.flagship.visitor;

import com.abtasty.flagship.main.ConfigManager;
import com.abtasty.flagship.utils.FlagshipLogManager;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Visitor abstract class
 */
public abstract class AbstractVisitor {

    public      abstract    String                      getId();
    public      abstract    HashMap<String, Object>     getContext();
    public      abstract    Boolean                     hasConsented();
    public      abstract    void                        setConsent(Boolean hasConsented);
    protected   abstract    void                        logVisitor(FlagshipLogManager.Tag tag);
    protected   abstract    ConfigManager               getConfigManager();
    protected   abstract    JSONObject                  getContextAsJson();
    protected   abstract    JSONObject                  getModificationsAsJson();
    protected   abstract    void                        clearVisitorData();
}
