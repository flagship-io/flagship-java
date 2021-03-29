package com.abtasty.demo;

import com.abtasty.flagship.hits.Screen;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.main.Visitor;

public class DemoFlagship {

    public static void main(String[] args)  {

        Flagship.start("bkk4s7gcmjcg07fke9dg", "Q6FDmj6F188nh75lhEato2MwoyXDS7y34VrAL4Aa", new FlagshipConfig());
//        Flagship.start("my_env_id", "my api key", new FlagshipConfig());

        Visitor visitor = Flagship.newVisitor("visitor1");
        visitor.updateContext("isVIPUser", true);

        while (true) {
            visitor.synchronizeModifications().whenComplete((Void, error) -> {
                Boolean displayFeature = visitor.getModification("featureEnabled", false, false);
                visitor.activateModification("featureEnabled");
            });
            visitor.sendHit(new Screen("main"));
        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        }
    }
}
