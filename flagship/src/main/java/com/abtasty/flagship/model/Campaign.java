package com.abtasty.flagship.model;

import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;

public class Campaign implements Serializable {

    private final String                        id;
    private final LinkedList<VariationGroup>    variationGroups;

    public Campaign(String id, LinkedList<VariationGroup> variationGroups) {
        this.id = id;
        this.variationGroups = variationGroups;
    }

    public String getId() {
        return id;
    }

    public LinkedList<VariationGroup> getVariationGroups() {
        return variationGroups;
    }

    public static ArrayList<Campaign> parse(JSONArray campaignsArray) {
        try {
            ArrayList<Campaign> campaigns = new ArrayList<>();
            campaignsArray.forEach(campaignObject -> {
                Campaign campaign = Campaign.parse((JSONObject) campaignObject);
                if (campaign != null)
                    campaigns.add(campaign);
            });
            return campaigns;
        } catch (Exception e){
            FlagshipLogManager.log(FlagshipLogManager.Tag.PARSING, LogManager.Level.ERROR, FlagshipConstants.Errors.PARSING_CAMPAIGN_ERROR);
            return null;
        }
    }

    public static Campaign parse(JSONObject campaignObject) {
        try {
            String id = campaignObject.getString("id");
            LinkedList<VariationGroup> variationGroups = new LinkedList<>();
            JSONArray variationGroupArray = campaignObject.optJSONArray("variationGroups");
            if (variationGroupArray != null) {
                //bucketing
                variationGroupArray.forEach(variationGroupsObj -> {
                    VariationGroup variationGroup = VariationGroup.parse(id, (JSONObject) variationGroupsObj, true);
                    if (variationGroup != null)
                        variationGroups.add(variationGroup);
                });
            } else {
                //api
                VariationGroup variationGroup = VariationGroup.parse(id, campaignObject, false);
                if (variationGroup != null)
                    variationGroups.add(variationGroup);
            }
            return new Campaign(id, variationGroups);
        }
        catch (Exception e){
            FlagshipLogManager.log(FlagshipLogManager.Tag.PARSING, LogManager.Level.ERROR, FlagshipConstants.Errors.PARSING_CAMPAIGN_ERROR);
            return null;
        }
    }

    @Override
    public String toString() {
        return "Campaign{" +
                "id='" + id + '\'' +
                ", variationGroups=" + variationGroups +
                '}';
    }
}

