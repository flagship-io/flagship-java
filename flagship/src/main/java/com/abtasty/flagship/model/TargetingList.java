package com.abtasty.flagship.model;

import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class TargetingList implements Serializable {

    private final ArrayList<Targeting> targetingList;

    TargetingList(ArrayList<Targeting> targetingList) {
        this.targetingList = targetingList;
    }

    public static TargetingList parse(JSONObject jsonObject) {
        try {
            ArrayList<Targeting> targetingList = new ArrayList<>();
            JSONArray targetingArray = jsonObject.getJSONArray("targetings");
            for (int i = 0; i < targetingArray.length(); i++) {
                Targeting targeting = Targeting.parse(targetingArray.getJSONObject(i));
                if (targeting != null)
                    targetingList.add(targeting);
            }
            return new TargetingList(targetingList);
        } catch (Exception e) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.PARSING, LogManager.Level.ERROR, FlagshipConstants.Errors.PARSING_TARGETING_ERROR);
            return null;
        }
    }

    public ArrayList<Targeting> getTargetingList() {
        return targetingList;
    }

    public Boolean isTargetingValid(HashMap<String, Object> context) {
        if (targetingList != null) {
            for (Targeting targeting : targetingList) {
                if (!targeting.isTargetingValid(context))
                    return false;
            }
        }
        return true;
    }
}
