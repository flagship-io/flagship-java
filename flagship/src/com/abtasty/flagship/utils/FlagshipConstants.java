package com.abtasty.flagship.utils;

public final class FlagshipConstants {

    public static class Errors {
        public static String INITIALIZATION_PARAM_ERROR = "Params 'envId' and 'apiKey' must not be null.";
        public static String INITIALIZATION_PARAM_ERROR_CONFIG = "Param 'config' must not be null.";
        public static String ERROR = "error";
        public static String VISITOR = "'%s' \n%s";
        public static String CONTEXT_PARAM_ERROR = "params 'key' must be a non null String, and 'value' must be one of the " +
                "following types : String, Number, Boolean, JsonObject, JsonArray.";
        public static String PARSING_ERROR = "an error occured whil parsing ";
        public static String PARSING_CAMPAIGN_ERROR = PARSING_ERROR + " campaign.";
        public static String PARSING_VARIATIONGROUP_ERROR = PARSING_ERROR + " variation group.";
        public static String PARSING_VARIATION_ERROR = PARSING_ERROR + " variation.";
        public static String PARSING_MODIFICATION_ERROR = PARSING_ERROR + " modification.";
        public static String PARSING_TARGETING_ERROR = PARSING_ERROR + " targeting.";
        public static String PARSING_VALUE_ERROR = PARSING_ERROR + " modification.";
        public static String GET_MODIFICATION_CAST_ERROR = "Modification for key '%s' has a different type. Default value is returned.";
        public static String GET_MODIFICATION_MISSING_ERROR = "No modification for key '%s'. Default value is returned.";
        public static String GET_MODIFICATION_KEY_ERROR = "Key '%s' must not be null. Default value is returned.";
        public static String GET_MODIFICATION_ERROR = "An error occured while retreiving modification for key '%s'. Default value is returned.";
        public static String HIT_INVALID_DATA_ERROR = "'%s' hit invalid format error. \n %s";
    }

    public static class HitKeyMap {
        public static String TYPE= "t";
        public static String CLIENT_ID= "cid";
        public static String VISITOR_ID= "vid";
        public static String CUSTOM_VISITOR_ID= "cvid";
        public static String DATA_SOURCE= "ds";
        public static String APP= "APP";

        public static String VARIATION_GROUP_ID= "caid";
        public static String VARIATION_ID= "vaid";

        public static String DOCUMENT_LOCATION= "dl";
        //        TITLE= "pt";
        public static String DOCUMENT= "dr";

        public static String TRANSACTION_ID= "tid";
        public static String TRANSACTION_AFFILIATION= "ta";
        public static String TRANSACTION_REVENUE= "tr";
        public static String TRANSACTION_SHIPPING= "ts";
        public static String TRANSACTION_TAX= "tt";
        public static String TRANSACTION_CURRENCY= "tc";
        public static String TRANSACTION_PAYMENT_METHOD= "pm";
        public static String TRANSACTION_SHIPPING_METHOD= "sm";
        public static String TRANSACTION_ITEM_COUNT= "icn";
        public static String TRANSACTION_COUPON= "tcc";

        public static String ITEM_NAME= "in";
        public static String ITEM_PRICE= "ip";
        public static String ITEM_QUANTITY= "iq";
        public static String ITEM_CODE= "ic";
        public static String ITEM_CATEGORY= "iv";

        public static String EVENT_CATEGORY= "ec";
        public static String EVENT_ACTION= "ea";
        public static String EVENT_LABEL= "el";
        public static String EVENT_VALUE= "ev";

        public static String DEVICE_RESOLUTION= "sr";
        public static String DEVICE_LOCALE= "ul";
        public static String TIMESTAMP= "cst";
        public static String SESSION_NUMBER= "sn";
        public static String IP= "uip";
        public static String QUEUE_TIME= "qt";

        public static String HIT_BATCH= "h";
    }

}
