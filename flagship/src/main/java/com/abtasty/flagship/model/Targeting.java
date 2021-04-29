package com.abtasty.flagship.model;

import com.abtasty.flagship.utils.ETargetingComp;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONObject;

import java.io.Serializable;

public class Targeting implements Serializable {

    String key;
    Object value;
    String operator;

    public Targeting(String key, Object value, String operator) {
        this.key = key;
        this.value = value;
        this.operator = operator;
    }

    public static Targeting parse(JSONObject jsonObject) {
        try {
            String key = jsonObject.getString("key");
            Object value = jsonObject.get("value");
            String operator = jsonObject.getString("operator");
            return new Targeting(key, value, operator);
        } catch (Exception e) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.PARSING, LogManager.Level.ERROR, FlagshipConstants.Errors.PARSING_TARGETING_ERROR);
            return null;
        }
    }

    public boolean isTargetingValid() {
        try {
            Object contextValue = null; //Todo context value
            if (contextValue == null)
                return false;
            else
                return ETargetingComp.get(operator).compare(contextValue, value);
        } catch (Exception e) {
            return  false;
        }
    }
}
