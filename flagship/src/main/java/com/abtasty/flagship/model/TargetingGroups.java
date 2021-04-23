package com.abtasty.flagship.model;

import org.json.JSONArray;

import java.io.Serializable;

public class TargetingGroups implements Serializable {

    public static TargetingGroups parse(JSONArray targetingGroupArr) {
        return new TargetingGroups();
    }
}
