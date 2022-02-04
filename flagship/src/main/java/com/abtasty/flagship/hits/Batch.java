package com.abtasty.flagship.hits;

import com.abtasty.flagship.utils.FlagshipConstants;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

public class Batch extends Hit<Batch> {

    public static final int MAX_SIZE = 2500;

    public Batch() {
        super(Type.BATCH);
        this.data.put(FlagshipConstants.HitKeyMap.HIT_BATCH, new JSONArray());
    }


    public void addChildAsJson(JSONObject child) {
        if (child.has(FlagshipConstants.HitKeyMap.TYPE) && child.has(FlagshipConstants.HitKeyMap.QUEUE_TIME))
            this.data.getJSONArray(FlagshipConstants.HitKeyMap.HIT_BATCH).put(child);
    }

    public boolean isMaxSizeReached(int lengthToAdd) {
        return (MAX_SIZE - this.data.length()) > lengthToAdd;
    }

    @Override
    public boolean checkData() {
        try {
            if (!Objects.equals(data.optString(FlagshipConstants.HitKeyMap.TYPE, ""), Type.BATCH.toString()))
                return false;
            JSONArray array = data.getJSONArray(FlagshipConstants.HitKeyMap.HIT_BATCH);
            if (array.length() == 0) return false;
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                item.get(FlagshipConstants.HitKeyMap.TYPE);
                item.get(FlagshipConstants.HitKeyMap.QUEUE_TIME);
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
