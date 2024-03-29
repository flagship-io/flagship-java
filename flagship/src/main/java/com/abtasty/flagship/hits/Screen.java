package com.abtasty.flagship.hits;

import com.abtasty.flagship.utils.FlagshipConstants;


public class Screen extends Hit<Screen> {

    /**
     * Hit to send when a user sees a client interface.
     *
     * @param location interface name
     */
    public Screen(String location) {
        super(Type.SCREENVIEW);
        this.data.put(FlagshipConstants.HitKeyMap.DOCUMENT_LOCATION, location);
    }

    @Override
    public boolean checkData() {
      return true;
    }
}