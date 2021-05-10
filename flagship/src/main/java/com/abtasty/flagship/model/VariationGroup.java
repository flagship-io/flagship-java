package com.abtasty.flagship.model;

import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import com.abtasty.flagship.utils.MurmurHash;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class VariationGroup implements Serializable {

    private final String                            campaignId;
    private final String                            variationGroupId;
    private final LinkedHashMap<String, Variation>  variations;
    private final TargetingGroups                   targetingGroups;
    private String                                  selectedVariationId;

    public VariationGroup(String campaignId, String variationGroupId, LinkedHashMap<String, Variation> variations, TargetingGroups targetingGroups, String selectedVariationId) {
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

    public Variation getSelectedVariation() {
        if (selectedVariationId != null && variations != null)
            return variations.get(selectedVariationId);
        return null;
    }

    public void selectVariation(String visitorId) {
//        if (selectedVariationId == null) //todo load from cache except if no consent
//            selectedVariationId = loadFromCache();
        if (selectedVariationId == null && variations != null) {
            int p = 0;
            int murmurAllocation = MurmurHash.getAllocationFromMurmur(variationGroupId, visitorId);
            for (Map.Entry<String, Variation> e : variations.entrySet()) {
                Variation variation = e.getValue();
                p += variation.getAllocation();
                if (murmurAllocation < p) {
                    selectedVariationId = variation.getVariationId();
                    variation.setSelected(true);
                    FlagshipLogManager.log(FlagshipLogManager.Tag.ALLOCATION, LogManager.Level.DEBUG,
                            String.format(FlagshipConstants.Info.NEW_ALLOCATION, variation.getVariationId(),
                                    murmurAllocation));
                    //saveToCache() //todo save in cache except if no consent
                    break;
                }
            }
        }
    }

    public boolean isTargetingValid(HashMap<String, Object> context) {
        if (targetingGroups != null)
            return targetingGroups.isTargetingValid(context);
        return true;
    }

    public static VariationGroup parse(String campaignId, JSONObject variationGroupsObj, boolean bucketing) {
        try {
            String variationGroupId = variationGroupsObj.getString(bucketing ? "id" : "variationGroupId");
            String selectedVariationId = null;
            TargetingGroups targetingGroups = null;
            LinkedHashMap<String, Variation> variations = new LinkedHashMap<String, Variation>();
            if (!bucketing) {
                // api
                JSONObject variationObj = variationGroupsObj.getJSONObject("variation");
                Variation variation = Variation.parse(campaignId, variationGroupId, variationObj);
                if (variation != null) {
                    variation.setSelected(true);
                    selectedVariationId = variation.getVariationId();
                    variations.put(variation.getVariationId(), variation);
                }
            } else {
                //bucketing
                JSONArray variationArr = variationGroupsObj.optJSONArray("variations");
                if (variationArr != null) {
                    for (int i = 0; i < variationArr.length(); i++) {
                        JSONObject variationObj = variationArr.getJSONObject(i);
                        if (variationObj.has("allocation")) {
                            Variation variation = Variation.parse(campaignId, variationGroupId, variationObj);
                            if (variation != null)
                                variations.put(variation.getVariationId(), variation);
                        }
                    }
                    JSONObject targetingObj = variationGroupsObj.optJSONObject("targeting");
                    if (targetingObj != null) {
                        JSONArray targetingArr = targetingObj.optJSONArray("targetingGroups");
                        if (targetingArr != null) {
                            targetingGroups = TargetingGroups.parse(targetingArr);
                        }
                    }
                }
            }
            return new VariationGroup(campaignId, variationGroupId, variations, targetingGroups, selectedVariationId);
        } catch (Exception e) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.PARSING, LogManager.Level.ERROR, FlagshipConstants.Errors.PARSING_VARIATION_GROUP_ERROR);
            return null;
        }
    }
}
