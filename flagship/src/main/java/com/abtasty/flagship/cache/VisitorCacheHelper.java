package com.abtasty.flagship.cache;

import com.abtasty.flagship.model.Modification;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import com.abtasty.flagship.visitor.VisitorDelegate;
import com.abtasty.flagship.visitor.VisitorDelegateDTO;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class VisitorCacheHelper extends CacheHelper {

    public static final int _VISITOR_CACHE_VERSION_ = 1;

    interface CacheVisitorMigrationInterface {
        void applyFromJSON(VisitorDelegateDTO visitorDelegateDTO, JSONObject data);
    }

    enum VisitorMigrations implements CacheVisitorMigrationInterface {

        MIGRATION_1() {
            @Override
            public void applyFromJSON(VisitorDelegateDTO visitorDelegateDTO, JSONObject data) {
                JSONObject dataObject = data.getJSONObject("data");
                VisitorDelegate visitorDelegate = visitorDelegateDTO.getVisitorDelegate();
                if (dataObject.getString("visitorId").equals(visitorDelegate.getVisitorId())) {
                    visitorDelegate.setVisitorId(dataObject.optString("visitorId"));
                    visitorDelegate.setAnonymousId(dataObject.optString("anonymousId", null));
                    visitorDelegate.setConsent(dataObject.optBoolean("consent", true));
                    JSONObject jsonContext = data.optJSONObject("context");
                    if (jsonContext != null) {
                        jsonContext.keySet().forEach(key -> {
                            visitorDelegate.getContext().put(key, jsonContext.get(key));
                        });
                    }
                    JSONArray campaignsArray = dataObject.optJSONArray("campaigns");
                    if (campaignsArray != null) {
                        for (int i = 0; i < campaignsArray.length(); i++) {
                            JSONObject campaignObject = campaignsArray.getJSONObject(i);
                            String campaignId = campaignObject.getString("campaignId");
                            String variationGroupId = campaignObject.getString("variationGroupId");
                            String variationId = campaignObject.getString("variationId");
                            boolean isReference = campaignObject.getBoolean("isReference");
                            String type = campaignObject.getString("type");
                            if (campaignObject.optBoolean("activated", false) && !visitorDelegate.getActivatedVariations().contains(variationId))
                                visitorDelegate.getActivatedVariations().add(variationId);
                            JSONObject flagsJson = campaignObject.optJSONObject("flags");
                            if (flagsJson != null) {
                                flagsJson.keySet().forEach(key -> {
                                    Modification modification = new Modification(key, campaignId, variationGroupId, variationId, isReference, flagsJson.get(key), type);
                                    visitorDelegate.getModifications().put(key, modification);
                                });
                            }
                        }
                    }
                    JSONObject assignmentsObject = dataObject.optJSONObject("assignmentsHistory");
                    if (assignmentsObject != null) {
                        for (String key : assignmentsObject.keySet()) {
                            visitorDelegate.getAssignmentsHistory().put(key, assignmentsObject.getString(key));
                        }
                    }
                }
            }
        }
    }

    public static JSONObject visitorToCacheJSON(VisitorDelegateDTO visitorDelegateDTO) {
        JSONObject data = new JSONObject()
                .put("visitorId", visitorDelegateDTO.getVisitorId())
                .put("anonymousId", visitorDelegateDTO.getAnonymousId())
                .put("consent", visitorDelegateDTO.hasConsented())
                .put("context", visitorDelegateDTO.contextToJson())
                .put("campaigns", modificationsToCacheJSON(visitorDelegateDTO))
                .put("assignmentsHistory", assignationHistoryToCacheJSON(visitorDelegateDTO));
        return new JSONObject()
                .put("version", _VISITOR_CACHE_VERSION_)
                .put("data", data);
    }

    private static JSONArray modificationsToCacheJSON(VisitorDelegateDTO visitorDelegateDTO) {
        JSONArray campaigns = new JSONArray();
        for (Map.Entry<String, Modification> m : visitorDelegateDTO.getModifications().entrySet()) {
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
                        .put("activated", visitorDelegateDTO.getActivatedVariations().contains(m.getValue().getVariationId()))
                        .put("flags", new JSONObject().put(m.getValue().getKey(),
                                (m.getValue().getValue() != null) ? m.getValue().getValue() : JSONObject.NULL)));
            }
        }
        return campaigns;
    }

    private static JSONObject assignationHistoryToCacheJSON(VisitorDelegateDTO visitorDelegateDTO) {
        JSONObject assignationsJSON = new JSONObject();
        for (Map.Entry<String, String> e : visitorDelegateDTO.getAssignmentsHistory().entrySet()) {
            assignationsJSON.put(e.getKey(), e.getValue());
        }
        return assignationsJSON;
    }

    public static void applyCacheToVisitor(VisitorDelegateDTO visitorDelegateDTO, JSONObject data) {
        int version = 0;
        try {
            if (data.keys().hasNext()) {
                version = data.getInt("version");
                VisitorMigrations.values()[version - 1].applyFromJSON(visitorDelegateDTO, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            FlagshipLogManager.log(FlagshipLogManager.Tag.CACHE, LogManager.Level.ERROR,
                    String.format(FlagshipConstants.Errors.CACHE_IMPL_FORMAT_ERROR, "lookupVisitor", version, visitorDelegateDTO.getVisitorId()));
        }
    }
}
