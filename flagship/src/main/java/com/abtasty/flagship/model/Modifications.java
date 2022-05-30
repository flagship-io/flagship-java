package com.abtasty.flagship.model;

import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.Serializable;
import java.util.HashMap;

public class Modifications implements Serializable {

    private final String                          campaignId;
    private final String                          variationGroupId;
    private final String                          variationId;
    private final boolean                         isReference;
    private final String                          type;
    private final HashMap<String, Modification>   values;

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

    public static Modifications parse(String campaignId, String campaignType, String slug, String variationGroupId, String variationId, boolean isReference, JSONObject modificationsObj) {
        try {
//            String type = modificationsObj.getString("type");
            HashMap<String, Modification> values = new HashMap<>();
            JSONObject valueObj = modificationsObj.getJSONObject("value");
            valueObj.keySet().forEach(key -> {
                Object value = valueObj.isNull(key) ? null : valueObj.get(key);
                if (value instanceof Boolean || value instanceof Number || value instanceof String || value instanceof JSONObject || value instanceof JSONArray || value == null)
                    values.put(key, new Modification(key, campaignId, variationGroupId, variationId, isReference, value, campaignType, slug));
                else
                    FlagshipLogManager.log(FlagshipLogManager.Tag.PARSING, LogManager.Level.ERROR, FlagshipConstants.Errors.PARSING_MODIFICATION_ERROR);
            });
            return new Modifications(campaignId, variationGroupId, variationId, isReference, campaignType, values);
        } catch (Exception e) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.PARSING, LogManager.Level.ERROR, FlagshipConstants.Errors.PARSING_MODIFICATION_ERROR);
            return null;
        }
    }
}
