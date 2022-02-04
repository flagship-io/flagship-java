package com.abtasty.flagship.utils;

public final class FlagshipConstants {

    public static class Info {

        public static final String READY = "Flagship SDK (version: %s) READY";
        public static final String BUCKETING_INTERVAL = "Polling event.";
        public static final String NEW_ALLOCATION = "Variation %s selected with allocation %d.";
        public static final String CACHED_ALLOCATION = "Variation %s selected from cache.";
        public static final String STATUS_CHANGED = "SDK status has changed : %s.";
        public static final String SQLITE_CACHE_MANAGER_CACHE_VISITOR = "[cacheVisitor] Visitor '%s' has been saved into database:\n%s.";
        public static final String SQLITE_CACHE_MANAGER_LOOKUP_VISITOR = "[lookupVisitor] Visitor '%s' has been loaded from database:\n%s.";
        public static final String SQLITE_CACHE_MANAGER_FLUSH_VISITOR = "[flushVisitor] Visitor '%s' has been flushed from database.";
        public static final String SQLITE_CACHE_MANAGER_CACHE_HIT = "[cacheHit] Hit for visitor '%s' has been saved into database:\n%s.";
        public static final String SQLITE_CACHE_MANAGER_LOOKUP_HIT = "[lookupHits] hits for visitor '%s' has been loaded from database:\n%s.";
        public static final String SQLITE_CACHE_MANAGER_FLUSH_HIT = "[flushHits] Hits for visitor '%s' has been flushed from database.";
        public static final String SQLITE_DATABASE_CREATION = "A new sqlite database has been created at: %s";
    }

    public static class Warnings {

        public static final String VISITOR_ID_NULL_OR_EMPTY = "Visitor identifier must not be null or empty. A UUID has been generated.";
        public static final String VISITOR_STATUS_NOT_READY = "New visitor '%s' has been created while SDK status is %s. Feature management will only be possible when SDK status is READY.";
        public static final String PANIC = "Panic mode is enabled : all feature are disabled except synchronization.";
        public static final String CONTEXT_VALUE_OVERRIDING = "key '%s' is overriding a predefined flagship value";
    }

    public static class Errors {

        public static final String INITIALIZATION_PARAM_ERROR = "Params 'envId' and 'apiKey' must not be null.";
        public static final String INITIALIZATION_PARAM_ERROR_CONFIG = "Param 'config' must not be null.";
        public static final String ERROR = "error";
        public static final String VISITOR = "'%s' \n%s";
        public static final String CONTEXT_KEY_ERROR = "param 'key' must be a non null String.";
        public static final String CONTEXT_VALUE_ERROR = "'value' for '%s', must be one of the following types : String, Number, Boolean";
//        public static final String CONTEXT_VALUE_ERROR = "param 'value' must be one of the following types : String, Number, Boolean, JsonObject, JsonArray";
        public static final String CONTEXT_RESERVED_KEY_ERROR = "key '%s' is reserved by flagship and can't be modified.";
        public static final String PARSING_ERROR = "an error occurred while parsing ";
        public static final String PARSING_CAMPAIGN_ERROR = PARSING_ERROR + " campaign.";
        public static final String PARSING_VARIATION_GROUP_ERROR = PARSING_ERROR + " variation group.";
        public static final String PARSING_VARIATION_ERROR = PARSING_ERROR + " variation.";
        public static final String PARSING_MODIFICATION_ERROR = PARSING_ERROR + " modification.";
        public static final String PARSING_TARGETING_ERROR = PARSING_ERROR + " targeting.";
        public static final String TARGETING_COMPARISON_ERROR = "Targeting %s %s %s has failed.";
        public static final String PARSING_VALUE_ERROR = PARSING_ERROR + " modification.";
        public static final String GET_MODIFICATION_CAST_ERROR = "Modification for key '%s' has a different type. Default value is returned.";
        public static final String GET_MODIFICATION_MISSING_ERROR = "No modification for key '%s'. Default value is returned.";
        public static final String GET_MODIFICATION_KEY_ERROR = "Key '%s' must not be null. Default value is returned.";
        public static final String GET_MODIFICATION_ERROR = "An error occurred while retrieving modification for key '%s'. Default value is returned.";
        public static final String GET_MODIFICATION_INFO_ERROR = "No modification for key '%s'.";
        public static final String HIT_INVALID_DATA_ERROR = "'%s' hit invalid format error. \n %s";
        public static final String METHOD_DEACTIVATED_ERROR = "Method '%s' is deactivated while SDK status is: %s.";
        public static final String METHOD_DEACTIVATED_CONSENT_ERROR = "Method '%s' is deactivated for visitor '%s': visitor did not consent.";
        public static final String CONFIGURATION_POLLING_ERROR = "Setting a polling interval is only available for Bucketing configuration.";
        public static final String AUTHENTICATION_BUCKETING_ERROR = "'%s' method will be ignored in Bucketing configuration.";

        public static final String CACHE_IMPL_ERROR  = "Error: '%s' for visitor '%s' threw an exception.";
        public static final String CACHE_IMPL_TIMEOUT = "Error: '%s' for visitor '%s' has timed out.";
        public static final String CACHE_IMPL_FORMAT_ERROR = "Error: '%s' have loaded a bad format version (%d) for visitor '%s'.";

        public static final String FLAG_ERROR = "Unexpected Error.";
        public static final String FLAG_CAST_ERROR = "Flag type and default value type are different.";
        public static final String FLAG_MISSING_ERROR = "Flag not found.";

        public static final String FLAG_VALUE_ERROR = "Default value will be returned for flag '%s': ";
        public static final String FLAG_USER_EXPOSITION_ERROR = "User exposition for Flag '%s' wont be sent: ";
        public static final String FLAG_METADATA_ERROR = "Empty metadata will be returned for Flag '%s': ";
    }

    public static class HitKeyMap {

        public static final String TYPE = "t";
        public static final String CLIENT_ID = "cid";
        public static final String VISITOR_ID = "vid";
        public static final String ANONYMOUS_ID = "aid";
        public static final String CUSTOM_VISITOR_ID = "cuid";
        public static final String DATA_SOURCE = "ds";
        public static final String APP = "APP";

        public static final String VARIATION_GROUP_ID = "caid";
        public static final String VARIATION_ID = "vaid";

        public static final String DOCUMENT_LOCATION = "dl";
        //        TITLE= "pt";
        public static String DOCUMENT = "dr";

        public static final String TRANSACTION_ID = "tid";
        public static final String TRANSACTION_AFFILIATION = "ta";
        public static final String TRANSACTION_REVENUE = "tr";
        public static final String TRANSACTION_SHIPPING = "ts";
        public static final String TRANSACTION_TAX = "tt";
        public static final String TRANSACTION_CURRENCY = "tc";
        public static final String TRANSACTION_PAYMENT_METHOD = "pm";
        public static final String TRANSACTION_SHIPPING_METHOD = "sm";
        public static final String TRANSACTION_ITEM_COUNT = "icn";
        public static final String TRANSACTION_COUPON = "tcc";

        public static final String ITEM_NAME = "in";
        public static final String ITEM_PRICE = "ip";
        public static final String ITEM_QUANTITY = "iq";
        public static final String ITEM_CODE = "ic";
        public static final String ITEM_CATEGORY = "iv";

        public static final String EVENT_CATEGORY = "ec";
        public static final String EVENT_ACTION = "ea";
        public static final String EVENT_LABEL = "el";
        public static final String EVENT_VALUE = "ev";

        public static final String DEVICE_RESOLUTION = "sr";
        public static final String DEVICE_LOCALE = "ul";
        public static String TIMESTAMP = "cst";
        public static final String SESSION_NUMBER = "sn";
        public static final String IP = "uip";
        public static String QUEUE_TIME = "qt";

        public static String HIT_BATCH = "h";

        public static String CONSENT = "co";
        public static String CONSENT_MECHANISM = "me";
    }

    public static class Exceptions {

        public static class FlagException extends Exception {
        }

        public static class FlagTypeException extends Exception {
        }

        public static class FlagNotFoundException extends Exception {
        }
    }
}
