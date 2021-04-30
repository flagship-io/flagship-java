package com.abtasty.flagship.model;

import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONArray;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class TargetingGroups implements Serializable {

    private final ArrayList<TargetingList> targetingGroups;

    TargetingGroups(ArrayList<TargetingList> targetingGroups) {
        this.targetingGroups = targetingGroups;
    }

    public static TargetingGroups parse(JSONArray targetingGroupArr) {
        try {
            ArrayList<TargetingList> targetingGroup = new ArrayList<TargetingList>();
            for (int i = 0; i < targetingGroupArr.length(); i++) {
                TargetingList targetingList = TargetingList.parse(targetingGroupArr.getJSONObject(i));
                if (targetingList != null)
                    targetingGroup.add(targetingList);
            }
            return new TargetingGroups(targetingGroup);
        } catch (Exception e) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.PARSING, LogManager.Level.ERROR, FlagshipConstants.Errors.PARSING_TARGETING_ERROR);
            return null;
        }
    }

    public ArrayList<TargetingList> getTargetingGroups() {
        return targetingGroups;
    }

    public Boolean isTargetingValid(HashMap<String, Object> context) {
        if (targetingGroups != null) {
            for (TargetingList group : targetingGroups) {
                if (group.isTargetingValid(context))
                    return true;
            }
        }
        return false;
    }
}
