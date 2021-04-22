package com.abtasty.demo;

import com.abtasty.flagship.hits.Screen;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.main.Visitor;
import com.abtasty.flagship.utils.LogManager;

import java.util.concurrent.TimeUnit;


public class DemoFlagship {

    public static void main(String[] args) {


        Flagship.start("bkk4s7gcmjcg07fke9dg", "Q6FDmj6F188nh75lhEato2MwoyXDS7y34VrAL4Aa",
                new FlagshipConfig()
                        .withLogLevel(LogManager.Level.ALL)
                        .withFlagshipMode(Flagship.Mode.BUCKETING)
                        .withBucketingPollingIntervals(2, TimeUnit.SECONDS)
        );
//        Flagship.start("my_env_id", "my api key", new FlagshipConfig().withLogLevel(LogManager.Level.ALL));

        Visitor visitor = Flagship.newVisitor("visitor1");

        visitor.updateContext("isVIP", true);

        visitor.synchronizeModifications().whenComplete((instance, error) -> {
            Boolean displayFeature = visitor.getModification("vipFeature", false, false);
            System.out.println("displayFeature = " + displayFeature);
            visitor.activateModification("vipFeature");
        });
        visitor.sendHit(new Screen("main"));

        try {
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
