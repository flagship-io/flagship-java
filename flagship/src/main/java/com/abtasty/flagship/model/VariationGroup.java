package com.abtasty.flagship.model;

import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONObject;
import java.io.Serializable;
import java.util.HashMap;

public class VariationGroup implements Serializable {

    private final String                        campaignId;
    private final String                        variationGroupId;
    private final HashMap<String, Variation>    variations;
    private final TargetingGroups               targetingGroups;
    private final String                        selectedVariationId;

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
            HashMap<String, Variation> variations = new HashMap<String, Variation>();
            JSONObject variationObj = variationGroupsObj.getJSONObject("variation");
            if (variationObj != null) {
                // api
                Variation variation = Variation.parse(campaignId, variationGroupId, variationObj);
                if (variation != null) {
                    variation.setSelected(true);
                    selectedVariationId = variation.getVariationId();
                    variations.put(variation.getVariationId(), variation);
                }
            } else {
                //bucketing
            }
            return new VariationGroup(campaignId, variationGroupId, variations, targetingGroups, selectedVariationId);
        } catch (Exception e) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.PARSING, LogManager.Level.ERROR, FlagshipConstants.Errors.PARSING_VARIATION_GROUP_ERROR);
            return null;
        }
    }
}
