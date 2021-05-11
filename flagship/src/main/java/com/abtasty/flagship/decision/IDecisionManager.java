package com.abtasty.flagship.decision;

import com.abtasty.flagship.model.Modification;
import com.abtasty.flagship.visitor.VisitorDelegate;

import java.util.HashMap;

public interface IDecisionManager {
    HashMap<String, Modification> getCampaignsModifications(VisitorDelegate visitor);
}
