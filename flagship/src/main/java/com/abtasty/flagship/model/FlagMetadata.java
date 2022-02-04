package com.abtasty.flagship.model;

import org.json.JSONObject;

public class FlagMetadata {

    public final String    campaignId;
    public final String    variationGroupId;
    public final String    variationId;
    public final boolean   isReference;
    public final String    campaignType;

    public FlagMetadata() {
        this.campaignId = "";
        this.variationGroupId = "";
        this.variationId = "";
        this.isReference = false;
        this.campaignType = "";
    }

    public FlagMetadata(String campaignId, String variationGroupId, String variationId, Boolean isReference, String campaignType) {
        this.campaignId = campaignId;
        this.variationGroupId = variationGroupId;
        this.variationId = variationId;
        this.isReference = isReference;
        this.campaignType = campaignType;
    }

    public static FlagMetadata fromModification(Modification modification) {
        if (modification == null)
            return new FlagMetadata();
        else {
            return new FlagMetadata(modification.getCampaignId(), modification.getVariationGroupId(),
                    modification.getVariationId(), modification.isReference(), modification.getType());
        }
    }

    public boolean exists() {
        return (!campaignId.isEmpty() && !variationGroupId.isEmpty() && !variationId.isEmpty());
    }

    public JSONObject toJSON() {
        if (!exists())
            return new JSONObject();
        else {
            return new JSONObject()
                    .put("campaignId", campaignId)
                    .put("variationGroupId", variationGroupId)
                    .put("variationId", variationId)
                    .put("isReference", isReference)
                    .put("campaignType", campaignType);
        }
    }
}
