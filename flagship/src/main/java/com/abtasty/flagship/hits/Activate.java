package com.abtasty.flagship.hits;

import com.abtasty.flagship.model.Modification;
import com.abtasty.flagship.utils.FlagshipConstants;

/**
 * Internal Hit for activations
 */
public class Activate extends Hit<Activate> {

    public Activate(Modification modification) {
        super(Type.ACTIVATION);
        this.data.put(FlagshipConstants.HitKeyMap.VARIATION_GROUP_ID, modification.getVariationGroupId());
        this.data.put(FlagshipConstants.HitKeyMap.VARIATION_ID, modification.getVariationId());
    }

    @Override
    public boolean checkData() {
        return true;
    }
}