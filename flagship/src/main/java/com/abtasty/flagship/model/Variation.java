package com.abtasty.flagship.model;

import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.logging.Level;

public class Variation implements Serializable {

    private String          campaignId;
    private String          variationGroupId;
    private String          variationId;
    private boolean         isReference;
    private Modifications   modifications;
    private int             allocation = 100;
    private boolean         isSelected = false;



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

    public int getAllocation() {
        return allocation;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public static Variation parse(String campaignId, String variationGroupId, JSONObject variationObj) {
        try {
            String variationId = variationObj.getString("id");
            boolean isReference = variationObj.getBoolean("reference");
            Modifications modifications = Modifications.parse(campaignId, variationGroupId, variationId, isReference, variationObj.getJSONObject("modifications"));
            int allocation = variationObj.optInt("allocation", -1);
            return new Variation(campaignId, variationGroupId, variationId, isReference, modifications, allocation);
        } catch (Exception e) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.PARSING, Level.SEVERE, FlagshipConstants.Errors.PARSING_VARIATION_ERROR);
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
                ", isSelected=" + isSelected +
                '}';
    }
}
