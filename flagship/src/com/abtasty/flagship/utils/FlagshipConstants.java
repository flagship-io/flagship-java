package com.abtasty.flagship.utils;

public final class FlagshipConstants {

    public static String INITIALIZATION_PARAM_ERROR = "Params 'envId' and 'apiKey' must not be null.";
    public static String INITIALIZATION_PARAM_ERROR_CONFIG = "Param 'config' must not be null.";
    public static String ERROR = "error";
    public static String VISITOR = "Visitor '%s' = %s";
    public static String CONTEXT_PARAM_ERROR = "params 'key' must be a non null String, and 'value' must be one of the " +
            "following types : String, Number, Boolean, JsonObject, JsonArray.";
    public static String PARSING_ERROR = "an error occured whil parsing ";
    public static String PARSING_CAMPAIGN_ERROR = PARSING_ERROR + " campaign.";
    public static String PARSING_VARIATIONGROUP_ERROR = PARSING_ERROR + " variation group.";
    public static String PARSING_VARIATION_ERROR = PARSING_ERROR + " variation.";
    public static String PARSING_MODIFICATION_ERROR = PARSING_ERROR + " modification.";
    public static String PARSING_TARGETING_ERROR = PARSING_ERROR + " targeting.";
    public static String PARSING_VALUE_ERROR = PARSING_ERROR + " modification.";

}
