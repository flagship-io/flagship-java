package com.abtasty.flagship.api;

public interface IFlagshipEndpoints {
    String SEP = "/";
    String DECISION_API = "https://decision.flagship.io/v2/";
    String CAMPAIGNS = "/campaigns/?exposeAllKeys=true"; // call to /event not needed in api mode
//  String CAMPAIGNS = "/campaigns/?exposeAllKeys=true&sendContextEvent=false"; // call to /event needed in api mode
    String ARIANE = "https://ariane.abtasty.com";
    String ACTIVATION = "activate";
    String EVENTS = "/events";
}
