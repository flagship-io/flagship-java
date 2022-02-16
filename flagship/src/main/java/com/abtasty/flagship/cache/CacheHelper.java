package com.abtasty.flagship.cache;

import com.abtasty.flagship.hits.Batch;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import com.abtasty.flagship.visitor.VisitorDelegate;
import com.abtasty.flagship.visitor.VisitorDelegateDTO;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class CacheHelper {

    public static final int _VISITOR_CACHE_VERSION_ = 1;
    public static final int _HIT_CACHE_VERSION_ = 1;
    public static final long _HIT_EXPIRATION_MS_ = 14400000;

    interface CacheVisitorMigrationInterface {
        void applyFromJSON(VisitorDelegate visitor, JSONObject data);
    }

    interface CacheHitMigrationInterface {
        JSONObject applyForBatch(VisitorDelegateDTO visitorDelegateDTO, JSONObject data);
        void applyForEvent(VisitorDelegateDTO visitorDelegateDTO, JSONObject data);
    }

    enum VisitorMigrations implements CacheVisitorMigrationInterface {
        MIGRATION_1() {
            @Override
            public void applyFromJSON(VisitorDelegate visitor, JSONObject data) {
                JSONObject dataJSON = data.getJSONObject("data");
                if (dataJSON.get("visitorId").equals(visitor.visitorId)) { // todo think anonymous
                    visitor.cachedVisitor.fromCacheJSON(dataJSON);
                }
//                JSONObject json = data.getJSONObject("data");
//                if (json.getString("visitorId").equals(visitor.visitorId)) {
//                    JSONObject jsonContext = json.optJSONObject("context");
//                    if (jsonContext != null) {
//                        jsonContext.keySet().forEach(key -> {
//                            visitor.getStrategy().updateContext(key, jsonContext.get(key));
//                        });
//                    }
//                    JSONArray campaignsArray = json.optJSONArray("campaigns");
//                    if (campaignsArray != null) {
//                       for (int i = 0; i < campaignsArray.length(); i++) {
//                           JSONObject campaignJson = campaignsArray.getJSONObject(i);
//                           String campaignId = campaignJson.getString("campaignId");
//                           String variationGroupId = campaignJson.getString("variationGroupId");
//                           String variationId = campaignJson.getString("variationId");
//                           boolean isReference = campaignJson.getBoolean("isReference");
//                           String type = campaignJson.getString("type");
//                           if (campaignJson.optBoolean("activated", false) && !visitor.activatedVariations.contains(variationId))
//                               visitor.activatedVariations.add(variationId);
//                           JSONObject flagsJson = campaignJson.optJSONObject("flags");
//                           if (flagsJson != null) {
//                               flagsJson.keySet().forEach(key -> {
//                                   Modification modification = new Modification(key, campaignId, variationGroupId, variationId, isReference, flagsJson.get(key), type);
//                                    visitor.modifications.put(key, modification);
//                               });
//                           }
//                       }
//                    }
//                }
            }
        };
    }

    enum HitMigrations implements CacheHitMigrationInterface {
        MIGRATION_1() {
            @Override
            public JSONObject applyForBatch(VisitorDelegateDTO visitorDelegateDTO, JSONObject data) {
                JSONObject jsonData = data.getJSONObject("data");
                if (jsonData.getString("visitorId").equals(visitorDelegateDTO.getVisitorId())) {
                    long time = jsonData.getLong("time");
                    if (System.currentTimeMillis() <= (time + _HIT_EXPIRATION_MS_)) {
                        JSONObject jsonContent = jsonData.getJSONObject("content");
                        jsonContent.remove(FlagshipConstants.HitKeyMap.CLIENT_ID);
                        jsonContent.remove(FlagshipConstants.HitKeyMap.VISITOR_ID);
                        jsonContent.remove(FlagshipConstants.HitKeyMap.CUSTOM_VISITOR_ID);
                        jsonContent.remove(FlagshipConstants.HitKeyMap.DATA_SOURCE);
                        jsonContent.remove(FlagshipConstants.HitKeyMap.DEVICE_LOCALE);
                        jsonContent.remove(FlagshipConstants.HitKeyMap.DEVICE_RESOLUTION);
                        jsonContent.put(FlagshipConstants.HitKeyMap.QUEUE_TIME, System.currentTimeMillis() - time);
                        return jsonContent;
                    }
                }
                return null;
            }

            @Override
            public void applyForEvent(VisitorDelegateDTO visitorDelegateDTO, JSONObject data) {
                JSONObject jsonData = data.getJSONObject("data");
                if (jsonData.get("visitorId").equals(visitorDelegateDTO.getVisitorId())) { // todo think anonymous
                    long time = jsonData.getLong("time");
                    String type = jsonData.getString("type");
                    JSONObject content = jsonData.getJSONObject("content");
                    if (System.currentTimeMillis() <= (time + _HIT_EXPIRATION_MS_))
                        visitorDelegateDTO.getConfigManager().getTrackingManager().sendHit(visitorDelegateDTO, type, time, content);
                }
            }
        };
    }

//    public static JSONObject fromVisitor(VisitorDelegateDTO visitorDelegateDTO) {
//        return new JSONObject().put("version", _VISITOR_CACHE_VERSION_)
//                .put("data", new JSONObject()
//                        .put("visitorId", visitorDelegateDTO.getVisitorId())
//                        .put("anonymousId", (visitorDelegateDTO.getAnonymousId() != null) ? visitorDelegateDTO.getAnonymousId() : JSONObject.NULL)
//                        .put("consent", visitorDelegateDTO.hasConsented())
//                        .put("context", visitorDelegateDTO.contextToJson())
//                        .put("campaigns", modificationAsJson(visitorDelegateDTO)));

//    }

    public static JSONObject fromHit(VisitorDelegateDTO visitorDelegateDTO, String type, JSONObject hitData, long time) {

        return new JSONObject()
                .put("version", _HIT_CACHE_VERSION_)
                .put("data", new JSONObject()
                        .put("time", (time > -1) ? time : System.currentTimeMillis())
                        .put("visitorId", visitorDelegateDTO.getVisitorId())
                        .put("anonymousId", (visitorDelegateDTO.getAnonymousId() != null) ? visitorDelegateDTO.getAnonymousId() : JSONObject.NULL)
                        .put("type", type)
                        .put("content", hitData));
    }

//    private static JSONArray modificationAsJson(VisitorDelegateDTO visitorDelegateDTO) {
//        JSONArray campaigns = new JSONArray();
//        for (Map.Entry<String, Modification> m : visitorDelegateDTO.getModifications().entrySet()) {
//            boolean isCampaignSet = false;
//            for (int i = 0; i < campaigns.length(); i++) {
//                JSONObject campaign = campaigns.getJSONObject(i);
//                if (Objects.equals(campaign.optString("campaignId"), m.getValue().getCampaignId()) &&
//                        Objects.equals(campaign.optString("variationGroupId"), m.getValue().getVariationGroupId()) &&
//                        Objects.equals(campaign.optString("variationId"), m.getValue().getVariationId())
//                ) {
//                    isCampaignSet = true;
//                    campaign.getJSONObject("flags").put(m.getValue().getKey(),
//                            (m.getValue().getValue() != null) ? m.getValue().getValue() : JSONObject.NULL);
//                }
//            }
//            if (!isCampaignSet) {
//                campaigns.put(new JSONObject()
//                        .put("campaignId", m.getValue().getCampaignId())
//                        .put("variationGroupId", m.getValue().getVariationGroupId())
//                        .put("variationId", m.getValue().getVariationId())
//                        .put("isReference", m.getValue().isReference())
//                        .put("type", m.getValue().getType())
//                        .put("activated", visitorDelegateDTO.isVariationAssigned(m.getValue().getVariationId()))
//                        .put("flags", new JSONObject().put(m.getValue().getKey(),
//                                (m.getValue().getValue() != null) ? m.getValue().getValue() : JSONObject.NULL)));
//            }
//        }
//        return campaigns;
//    }

    public static void applyVisitorMigration(VisitorDelegate visitorDelegate, JSONObject data) {
        int version = 0;

        try {
            if (data.keys().hasNext()) {
                version = data.getInt("version");
                VisitorMigrations.values()[version - 1].applyFromJSON(visitorDelegate, data);
            }
        } catch (Exception e) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.CACHE, LogManager.Level.ERROR,
                    String.format(FlagshipConstants.Errors.CACHE_IMPL_FORMAT_ERROR, "lookupVisitor", version, visitorDelegate.visitorId));
        }
    }

    private static Batch findFirstAvailableBatch(ArrayList<Batch> batches, int len) {
        for (int i = 0; i < batches.size(); i++) {
            if (batches.get(i).isMaxSizeReached(len))
                return batches.get(i);
        }
        return null;
    }

    public static void applyHitMigration(VisitorDelegateDTO visitorDelegateDTO, JSONArray result) {
        if (result.length() > 0) {
            ArrayList<Batch> batches = new ArrayList<Batch>();
            for (int i = 0; i < result.length(); i++) {
                JSONObject e = result.getJSONObject(i);
                int version = 0;
                try {
                    version = e.getInt("version");
                    if (version > 0) {
                        String type = e.getJSONObject("data").getString("type");
                        if (Arrays.asList("CONTEXT", "ACTIVATION", "BATCH").contains(type))
                            HitMigrations.values()[version -1].applyForEvent(visitorDelegateDTO, e);
                        else if (Arrays.asList("SCREENVIEW", "PAGEVIEW", "EVENT", "TRANSACTION", "ITEM", "CONSENT").contains(type)) {
                            JSONObject jsonChild = HitMigrations.values()[version -1].applyForBatch(visitorDelegateDTO, e);
                            if (jsonChild != null) {
                                Batch batch = findFirstAvailableBatch(batches, jsonChild.length());
                                if (batch == null) {
                                    Batch newBatch = new Batch();
                                    newBatch.addChildAsJson(jsonChild);
                                    batches.add(newBatch);
                                } else {
                                    batch.addChildAsJson(jsonChild);
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    FlagshipLogManager.log(FlagshipLogManager.Tag.CACHE, LogManager.Level.ERROR,
                            String.format(FlagshipConstants.Errors.CACHE_IMPL_FORMAT_ERROR, "lookupHits", version, visitorDelegateDTO.getVisitorId()));
                }
            }
            for (int j = 0; j < batches.size(); j++) {
                visitorDelegateDTO.getConfigManager().getTrackingManager().sendHit(visitorDelegateDTO, batches.get(j));
            }
        }
    }

}
