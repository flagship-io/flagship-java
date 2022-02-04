package com.abtasty.flagship.model;

import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import com.abtasty.flagship.utils.MurmurHash;
import com.abtasty.flagship.visitor.VisitorDelegate;
import com.abtasty.flagship.visitor.VisitorDelegateDTO;
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

    public VariationGroup(String campaignId, String variationGroupId, LinkedHashMap<String, Variation> variations, TargetingGroups targetingGroups) {
        this.campaignId = campaignId;
        this.variationGroupId = variationGroupId;
        this.targetingGroups = targetingGroups;
        this.variations = variations;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public String getVariationGroupId() {
        return variationGroupId;
    }

    public LinkedHashMap<String, Variation> getVariations() {
        return variations;
    }

    public TargetingGroups getTargetingGroups() {
        return targetingGroups;
    }

    public Variation selectVariation(VisitorDelegateDTO visitor) {
        if (variations != null) {
            Variation cachedVariation = selectVariationFromCache(visitor, variations);
            if (cachedVariation != null)
                return cachedVariation;
            else {
                int p = 0;
                int murmurAllocation = MurmurHash.getAllocationFromMurmur(variationGroupId, visitor.getVisitorId());
                for (Map.Entry<String, Variation> e : variations.entrySet()) {
                    Variation variation = e.getValue();
                    if (variation.getAllocation() > 0) { //Variation with 0% are only loaded to check if it matches one from the cache, and should be ignored otherwise.
                        p += variation.getAllocation();
                        if (murmurAllocation < p) {
                            FlagshipLogManager.log(FlagshipLogManager.Tag.ALLOCATION, LogManager.Level.DEBUG,
                                    String.format(FlagshipConstants.Info.NEW_ALLOCATION, variation.getVariationId(),
                                            murmurAllocation));

                            return variation;
                        }
                    }
                }
            }
        }
        return null;
    }

    private Variation selectVariationFromCache(VisitorDelegateDTO visitor, LinkedHashMap<String, Variation>  variations) {
        for (Map.Entry<String, Variation> e: variations.entrySet()) {
            Variation v = e.getValue();
            if (visitor.isVariationAssigned(v.getVariationId())) {
                FlagshipLogManager.log(FlagshipLogManager.Tag.ALLOCATION, LogManager.Level.DEBUG,
                        String.format(FlagshipConstants.Info.CACHED_ALLOCATION, v.getVariationId()));
                return v;
            }
        }
        return null;
    }

    public boolean isTargetingValid(HashMap<String, Object> context) {
        if (targetingGroups != null)
            return targetingGroups.isTargetingValid(context);
        return true;
    }

    public static VariationGroup parse(String campaignId, String campaignType, JSONObject variationGroupsObj, boolean bucketing) {
        try {
            String variationGroupId = variationGroupsObj.getString(bucketing ? "id" : "variationGroupId");
            TargetingGroups targetingGroups = null;
            LinkedHashMap<String, Variation> variations = new LinkedHashMap<>();
            if (!bucketing) {
                // api
                JSONObject variationObj = variationGroupsObj.getJSONObject("variation");
                Variation variation = Variation.parse(bucketing, campaignId, campaignType, variationGroupId, variationObj);
                if (variation != null)
                    variations.put(variation.getVariationId(), variation);
            } else {
                //bucketing
                JSONArray variationArr = variationGroupsObj.optJSONArray("variations");
                if (variationArr != null) {
                    for (int i = 0; i < variationArr.length(); i++) {
                        JSONObject variationObj = variationArr.getJSONObject(i);
                            Variation variation = Variation.parse(bucketing, campaignId, campaignType, variationGroupId, variationObj);
                            if (variation != null)
                                variations.put(variation.getVariationId(), variation);
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
            return new VariationGroup(campaignId, variationGroupId, variations, targetingGroups);
        } catch (Exception e) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.PARSING, LogManager.Level.ERROR, FlagshipConstants.Errors.PARSING_VARIATION_GROUP_ERROR);
            return null;
        }
    }

    @Override
    public String toString() {
        return "VariationGroup{" +
                "campaignId='" + campaignId + '\'' +
                ", variationGroupId='" + variationGroupId + '\'' +
                ", variations=" + variations +
                ", targetingGroups=" + targetingGroups +
                '}';
    }
}
