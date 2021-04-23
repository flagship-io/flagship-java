package com.abtasty.flagship.model;

import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import com.abtasty.flagship.utils.MurmurHash;
import org.json.JSONArray;
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
                //selectedVariation = loadFromCache()
                JSONArray variationArr = variationGroupsObj.optJSONArray("variations");
                if (variationArr != null) {
                    int p = 0;
//                    long murmur = MurmurHash.getAllocationFromMurmur(variationGroupId, "");
                    for (int i = 0; i < variationArr.length(); i++) {
                        JSONObject variationObj = variationArr.getJSONObject(i);
                        if (variationObj.has("allocation")) {
                            Variation variation = Variation.parse(campaignId, variationGroupId, variationObj);
                            if (variation != null) {
//                                if (selectedVariationId == null) {
//                                    p += variation.getAllocation();
//                                    if (murmur < p) {
//                                        selectedVariationId = variation.getVariationId();
//                                        variation.setSelected(true);
//                                        FlagshipLogManager.log(FlagshipLogManager.Tag.ALLOCATION, LogManager.Level.DEBUG,
//                                                String.format(FlagshipConstants.Info.NEW_ALLOCATION, variation.getVariationId(),
//                                                        murmur));
//                                        //Save in cache
//                                    }
//                                }
                                variations.put(variation.getVariationId(), variation);
                            }
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
