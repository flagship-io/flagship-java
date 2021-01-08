package com.abtasty.flagship.api;

import com.abtasty.flagship.main.FlagshipConfig;
import sun.net.www.http.HttpClient;

public class TrackingManager implements IFlagshipEndpoints {

    private FlagshipConfig config = null;

    TrackingManager(FlagshipConfig config) {
        this.config = config;
    }
}
