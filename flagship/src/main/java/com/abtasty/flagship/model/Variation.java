package com.abtasty.flagship.model;

import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONObject;
import java.io.Serializable;
import java.util.HashMap;

public class Variation implements Serializable {

    private final String            campaignId;
    private final String            variationGroupId;
    private final String            variationId;
    private final boolean           isReference;
    private final Modifications     modifications;
    private int                     allocation = 100;

    Variation(String campaignId, String variationGroupId, String variationId, boolean isReference, Modifications modifications, int allocation) {
        this.campaignId = campaignId;
        this.variationGroupId = variationGroupId;
        this.variationId = variationId;
        this.isReference = isReference;
        this.modifications = modifications;
        this.allocation = allocation;
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

    public Modifications getModifications() {
        return modifications;
    }

    public HashMap<String, Modification> getModificationsValues() {
        return (modifications != null) ? modifications.getValues() : null;
    }

    public int getAllocation() {
        return allocation;
    }

    public static Variation parse(Boolean bucketingMode, String campaignId, String campaignType, String slug, String variationGroupId, JSONObject variationObj) {
        try {
            String variationId = variationObj.getString("id");
            boolean isReference = variationObj.optBoolean("reference", false);
            Modifications modifications = Modifications.parse(campaignId, campaignType, slug, variationGroupId, variationId, isReference, variationObj.getJSONObject("modifications"));
            int allocation = variationObj.optInt("allocation", bucketingMode ? 0 : 100);
            //In Api mode always 100%, in bucketing mode the variations at 0% are loaded just to check if it matches one in cache at selection time.
            return new Variation(campaignId, variationGroupId, variationId, isReference, modifications, allocation);
        } catch (Exception e) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.PARSING, LogManager.Level.ERROR, FlagshipConstants.Errors.PARSING_VARIATION_ERROR);
            return null;
        }
    }

    @Override
    public String toString() {
        return "Variation{" +
                "campaignId='" + campaignId + '\'' +
                ", variationGroupId='" + variationGroupId + '\'' +
                ", variationId='" + variationId + '\'' +
                ", isReference=" + isReference +
                ", modifications=" + modifications +
                ", allocation=" + allocation +
                '}';
    }
}
