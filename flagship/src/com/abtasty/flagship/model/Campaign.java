package com.abtasty.flagship.model;

import com.abtasty.flagship.main.Flagship;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Campaign implements Serializable {

    String id;
    LinkedHashMap variationGroups = new LinkedHashMap<String, VariationGroup>();

    public Campaign() {
    }

    public static ArrayList<Campaign> parse(String json) {
        try {
            JSONObject main = new JSONObject(json);
            ArrayList<Campaign> campaigns = new ArrayList<>();
            JSONArray campaignArray = main.getJSONArray("campaigns");
            campaignArray.forEach(campaignObject -> {
                Campaign campaign = Campaign.parse((JSONObject) campaignObject);
                if (campaign != null)
                    campaigns.add(campaign);
            });
            return campaigns;
        } catch (Exception e){
            //todo log
            //Flagship.getConfig().logManager
            e.printStackTrace();
            return null;
        }
    }

    public static Campaign parse(JSONObject campaignObject) {
        try {
            return new Campaign();
        }
        catch (Exception e){
            return null;
        }
    }
}

