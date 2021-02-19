package com.abtasty.flagship.model;

import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.LogLevel;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;

public class VariationGroup implements Serializable {

    private String                      campaignId;
    private String                      variationGroupId;
    private HashMap<String, Variation>  variations = new HashMap<>();
    private TargetingGroups             targetingGroups;
    private String                      selectedVariationId;

    public VariationGroup(String campaignId, String variationGroupId, HashMap<String, Variation> variations, TargetingGroups targetingGroups, String selectedVariationId) {
        this.campaignId = campaignId;
        this.variationGroupId = variationGroupId;
        this.targetingGroups = targetingGroups;
        this.variations = variations;
        this.selectedVariationId = selectedVariationId;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public String getVariationGroupId() {
        return variationGroupId;
    }

    public HashMap<String, Variation> getVariations() {
        return variations;
    }

    public TargetingGroups getTargetingGroups() {
        return targetingGroups;
    }

    public String getSelectedVariationId() {
        return selectedVariationId;
    }

    public static VariationGroup parse(String campaignId, JSONObject variationGroupsObj, boolean bucketing) {
        try {
            String variationGroupId = variationGroupsObj.getString(bucketing ? "id" : "variationGroupId");
            String selectedVariationId = null;
            TargetingGroups targetingGroups = null;
            HashMap<String, Variation> variations = new HashMap();
            JSONObject variationObj = variationGroupsObj.getJSONObject("variation");
            if (variationObj != null) {
                // api
                Variation variation = Variation.parse(campaignId, variationGroupId, variationObj);
                variation.setSelected(true);
                selectedVariationId = variation.getVariationId();
                variations.put(variation.getVariationId(), variation);
            } else {
                //bucketing
            }
            return new VariationGroup(campaignId, variationGroupId, variations, targetingGroups, selectedVariationId);
        } catch (Exception e) {
            LogManager.log(LogManager.Tag.PARSING, LogLevel.ERROR, FlagshipConstants.Errors.PARSING_VARIATIONGROUP_ERROR);
            return null;
        }
    }
}
