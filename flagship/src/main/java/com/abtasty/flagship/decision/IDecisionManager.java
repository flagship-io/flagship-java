package com.abtasty.flagship.decision;

import com.abtasty.flagship.model.Modification;
import com.abtasty.flagship.visitor.VisitorDelegate;
import com.abtasty.flagship.visitor.VisitorDelegateDTO;

import java.util.HashMap;

public interface IDecisionManager {
    HashMap<String, Modification> getCampaignsModifications(VisitorDelegateDTO visitor);
}
