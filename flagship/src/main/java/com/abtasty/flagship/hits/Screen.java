package com.abtasty.flagship.hits;

import com.abtasty.flagship.utils.FlagshipConstants;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class Screen extends Hit<Screen> {

    public Screen(String location) {
        super(Type.SCREENVIEW);
        this.data.put(FlagshipConstants.HitKeyMap.DOCUMENT_LOCATION, location);
    }

    @Override
    public boolean checkData() {
      return true;
    }
}