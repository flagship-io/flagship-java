package com.abtasty.flagship.api;

public interface IFlagshipEndpoints {
    public static String SEP = "/";
    public static String DECISION_API = "https://decision.flagship.io/v2/";
    public static String CAMPAIGNS = "/campaigns/?exposeAllKeys=true"; // call to /event not needed in api mode
//    public static String CAMPAIGNS = "/campaigns/?exposeAllKeys=true&sendContextEvent=false"; // call to /event needed in api mode
    public static String ARIANE = "https://ariane.abtasty.com";
    public static String ACTIVATION = "activate";
    public static String EVENTS = "/events";
}
