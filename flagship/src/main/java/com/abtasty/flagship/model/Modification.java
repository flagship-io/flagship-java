package com.abtasty.flagship.model;

import java.io.Serializable;

public class Modification implements Serializable {

    private final String      key;
    private final String      campaignId;
    private final String      variationGroupId;
    private final String      variationId;
    private final boolean     isReference;
    private final Object      value;
    private final String      type;

    public Modification(String key, String campaignId, String variationGroupId, String variationId, boolean isReference,
                        Object value, String type) {
        this.key = key;
        this.campaignId = campaignId;
        this.variationGroupId = variationGroupId;
        this.variationId = variationId;
        this.isReference = isReference;
        this.value = value;
        this.type = type;
    }

    public String getKey() {
        return key;
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

    public Object getValue() {
        return value;
    }

    public String getType() {
        return type;
    }
}
