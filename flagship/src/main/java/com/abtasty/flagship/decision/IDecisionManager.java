package com.abtasty.flagship.decision;

import com.abtasty.flagship.main.visitor.Visitor;
import com.abtasty.flagship.model.Modification;
import java.util.HashMap;

public interface IDecisionManager {
    HashMap<String, Modification> getCampaignsModifications(Visitor visitor);
}
