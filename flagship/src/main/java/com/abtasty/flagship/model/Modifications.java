package com.abtasty.flagship.model;

import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.LogLevel;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

public class Modifications implements Serializable {

    private String                          campaignId;
    private String                          variationGroupId;
    private String                          variationId;
    private boolean                         isReference;
    private String                          type;
    private HashMap<String, Modification>   values;

    public Modifications(String campaignId, String variationGroupId, String variationId, boolean isReference, String type, HashMap<String, Modification> values) {
        this.campaignId = campaignId;
        this.variationGroupId = variationGroupId;
        this.variationId = variationId;
        this.isReference = isReference;
        this.type = type;
        this.values = values;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public String getVariationGroupId() {
        return variationGroupId;
    }

    public String getVariationId() {
        return variationId;
    }

    public boolean isReference() {
        return isReference;
    }

    public String getType() {
        return type;
    }

    public HashMap<String, Modification> getValues() {
        return values;
    }

    public static Modifications parse(String campaignId, String variationGroupId, String variationId, boolean isReference, JSONObject modificationsObj) {
        try {
            String type = modificationsObj.getString("type");
            HashMap<String, Modification> values = new HashMap<>();
            JSONObject valueObj = modificationsObj.getJSONObject("value");
            valueObj.keySet().forEach(key -> {
                Object value = valueObj.isNull(key) ? null : valueObj.get(key);
                if (value instanceof Boolean || value instanceof Number || value instanceof String || value instanceof JSONObject || value instanceof JSONArray || value == null)
                    values.put(key, new Modification(key, campaignId, variationGroupId, variationId, isReference, value));
                else
                    LogManager.log(LogManager.Tag.PARSING, LogLevel.ERROR, FlagshipConstants.Errors.PARSING_MODIFICATION_ERROR);
            });
            return new Modifications(campaignId, variationGroupId, variationId, isReference, type, values);
        } catch (Exception e) {
            LogManager.log(LogManager.Tag.PARSING, LogLevel.ERROR, FlagshipConstants.Errors.PARSING_MODIFICATION_ERROR);
            return null;
        }
    }
}