package com.abtasty.flagship.utils;

import org.json.JSONArray;

public interface ITargetingComp {

    boolean compareObjects(Object contextValue, Object flagshipValue);

    default boolean compareNumbers(Number contextValue, Number flagshipValue) {
        return compareObjects(contextValue, flagshipValue);
    }

    default boolean compareInJsonArray(Object contextValue, JSONArray flagshipValue) {
        return false;
    }

    default boolean compare(Object contextValue, Object flagshipValue) {
        try {
            if (flagshipValue instanceof JSONArray)
                return compareInJsonArray(contextValue, (JSONArray) flagshipValue);
            else if (contextValue instanceof Number && flagshipValue instanceof Number)
                return compareNumbers((Number) contextValue, (Number) flagshipValue);
            else if (contextValue.getClass().equals(flagshipValue.getClass()))
                return compareObjects(contextValue, flagshipValue);
            else
                return false;
        } catch (Exception e) {
//            FlagshipLogManager.log(FlagshipLogManager.Tag.TARGETING, LogManager.Level.ERROR, FlagshipConstants.Errors.TARGETING_COMPARISON_ERROR);
            return false;
        }
    }
}
