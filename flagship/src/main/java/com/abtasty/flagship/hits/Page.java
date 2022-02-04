package com.abtasty.flagship.hits;

import com.abtasty.flagship.utils.FlagshipConstants;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class Page extends Hit<Page> {

    /**
     * Hit to send when a user sees a web page.
     *
     * @param location page url.
     */
    public Page(String location) {
        super(Type.PAGEVIEW);
        this.data.put(FlagshipConstants.HitKeyMap.DOCUMENT_LOCATION, location);
    }

    @Override
    public boolean checkData() {
        String dl = this.data.optString(FlagshipConstants.HitKeyMap.DOCUMENT_LOCATION, null);
        try {
           new URL(dl).toURI();
           return true;
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
    }
}