package com.abtasty.flagship.visitor;

import com.abtasty.flagship.cache.CacheHelper;
import com.abtasty.flagship.model.Modification;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VisitorCache extends VisitorDelegateDTO {

    protected   HashMap<String, String>         assignmentsHistory;

    public VisitorCache(VisitorDelegate visitorDelegate) {
        super(visitorDelegate);
    }

    public void fromCacheJSON(JSONObject visitorCache) {
        this.visitorId = visitorCache.optString("visitorId");
        this.anonymousId = visitorCache.optString("anonymousId");
        this.hasConsented = visitorCache.optBoolean("consent");
        JSONObject jsonContext = visitorCache.optJSONObject("context");
        if (jsonContext != null) {
            jsonContext.keySet().forEach(key -> {
                this.context.put(key, jsonContext.get(key));
            });
        }
        JSONArray campaignsArray = visitorCache.optJSONArray("campaigns");
        if (campaignsArray != null) {
            for (int i = 0; i < campaignsArray.length(); i++) {
                JSONObject campaignJson = campaignsArray.getJSONObject(i);
                String campaignId = campaignJson.getString("campaignId");
                String variationGroupId = campaignJson.getString("variationGroupId");
                String variationId = campaignJson.getString("variationId");
                boolean isReference = campaignJson.getBoolean("isReference");
                String type = campaignJson.getString("type");
                if (campaignJson.optBoolean("activated", false) && !this.activatedVariations.contains(variationId))
                    this.activatedVariations.add(variationId);
                JSONObject flagsJson = campaignJson.optJSONObject("flags");
                if (flagsJson != null) {
                    flagsJson.keySet().forEach(key -> {
                        Modification modification = new Modification(key, campaignId, variationGroupId, variationId, isReference, flagsJson.get(key), type);
                        modifications.put(key, modification);
                    });
                }
            }
        }
        this.assignmentsHistory = getAssignmentsHistoryFromJSON(visitorCache.optJSONObject("assignmentsHistory"));
        //todo load context ?
    }

    private HashMap<String, String> getAssignmentsHistoryFromJSON(JSONObject json) {
        HashMap<String, String> assignments = new HashMap<>();
        for (String key : json.keySet()) {
            assignments.put(key, json.getString(key));
        }
        return assignments;
    }

    public JSONObject toCacheJSON() {
        JSONObject data = new JSONObject()
                .put("visitorId", visitorId)
                .put("anonymousId", anonymousId)
                .put("consent", hasConsented)
                .put("context", contextToJson())
                .put("campaigns", this.modificationsToCacheJSON())
                .put("assignmentsHistory", this.assignationHistoryToJSON());
        return new JSONObject()
                .put("version", CacheHelper._VISITOR_CACHE_VERSION_)
                .put("data", data);
    }

    private JSONArray modificationsToCacheJSON() {
        JSONArray campaigns = new JSONArray();
        for (Map.Entry<String, Modification> m : modifications.entrySet()) {
            boolean isCampaignSet = false;
            for (int i = 0; i < campaigns.length(); i++) {
                JSONObject campaign = campaigns.getJSONObject(i);
                if (Objects.equals(campaign.optString("campaignId"), m.getValue().getCampaignId()) &&
                        Objects.equals(campaign.optString("variationGroupId"), m.getValue().getVariationGroupId()) &&
                        Objects.equals(campaign.optString("variationId"), m.getValue().getVariationId())
                ) {
                    isCampaignSet = true;
                    campaign.getJSONObject("flags").put(m.getValue().getKey(),
                            (m.getValue().getValue() != null) ? m.getValue().getValue() : JSONObject.NULL);
                }
            }
            if (!isCampaignSet) {
                campaigns.put(new JSONObject()
                        .put("campaignId", m.getValue().getCampaignId())
                        .put("variationGroupId", m.getValue().getVariationGroupId())
                        .put("variationId", m.getValue().getVariationId())
                        .put("isReference", m.getValue().isReference())
                        .put("type", m.getValue().getType())
                        .put("activated", activatedVariations.contains(m.getValue().getVariationId()))
                        .put("flags", new JSONObject().put(m.getValue().getKey(),
                                (m.getValue().getValue() != null) ? m.getValue().getValue() : JSONObject.NULL)));
            }
        }
        return campaigns;
    }

    private JSONObject assignationHistoryToJSON() {
        JSONObject assignationsJSON = new JSONObject();
        for (Map.Entry<String, String> e : assignmentsHistory.entrySet()) {
            assignationsJSON.put(e.getKey(), e.getValue());
        }
        return assignationsJSON;
    }

    public String getVariationGroupAssignment(String variationGroupId) {
        return assignmentsHistory.getOrDefault(variationGroupId, null);
    }

    public void addNewAssignment(String variationGroupId, String variationId) {
//        if (!assignmentsHistory.containsKey(variationGroupId)) //might be not compatible with XPC
            assignmentsHistory.put(variationGroupId, variationId);
    }
}
