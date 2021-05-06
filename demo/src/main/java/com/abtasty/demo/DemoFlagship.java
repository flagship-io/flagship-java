package com.abtasty.demo;

import com.abtasty.flagship.hits.Screen;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.visitor.Visitor;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class DemoFlagship {

    public static void main(String[] args) throws InterruptedException {

        CountDownLatch flagshipReadyLatch = new CountDownLatch(1);

        Flagship.start("bkk4s7gcmjcg07fke9dg", "Q6FDmj6F188nh75lhEato2MwoyXDS7y34VrAL4Aa",
                new FlagshipConfig()
                        .withLogLevel(LogManager.Level.ALL)
                        .withFlagshipMode(Flagship.Mode.BUCKETING)
                        .withBucketingPollingIntervals(20, TimeUnit.SECONDS)
                        .withStatusListener(newStatus -> {
                            System.out.println("NEW STATUS = " + newStatus.name());
                            if (newStatus == Flagship.Status.READY)
                                flagshipReadyLatch.countDown();
                        })
        );
//        Flagship.start("my_env_id", "my api key", new FlagshipConfig().withLogLevel(LogManager.Level.ALL));

        flagshipReadyLatch.await();

        Visitor visitor = Flagship.newVisitor("xx_visitor1963");

        visitor.updateContext("isVIP", true);

        visitor.synchronizeModifications().whenComplete((instance, error) -> {
            Boolean displayFeature = visitor.getModification("vipFeature", false, false);
            System.out.println("displayFeature = " + displayFeature);
            visitor.activateModification("vipFeature");
        });
        visitor.sendHit(new Screen("main"));

        Thread.sleep(2000);

        visitor.updateContext("isVIPUser", true);
        visitor.updateContext(new HashMap<String, Object>() {{
            put("daysSinceLastLaunch", 5);
            put("sdk_deviceModel", "Pixel X");
        }});

        visitor.synchronizeModifications().whenComplete((instance, error) -> {

        });


        //////////

        visitor.updateContext("isVIPUser", false);
        visitor.updateContext(new HashMap<String, Object>() {{
            put("daysSinceLastLaunch", 6);
            put("sdk_deviceModel", "coucou");
        }});

        visitor.synchronizeModifications().whenComplete((instance, error) -> {

        });

        JSONObject featureEnabled = visitor.getModificationInfo("featureEnabled");
        JSONObject isref = visitor.getModificationInfo("isref");
        JSONObject all_users = visitor.getModificationInfo("all_users");
        JSONObject release = visitor.getModificationInfo("release");
        JSONObject target = visitor.getModificationInfo("target");

        System.out.println("featureEnabled = " + featureEnabled);
        System.out.println("isref = " + isref);
        System.out.println("all_users = " + all_users);
        System.out.println("release = " + release);
        System.out.println("target = " + target);

        visitor.activateModification("featureEnabled");
        visitor.activateModification("isref");
        visitor.activateModification("all_users");
        visitor.activateModification("release");
        visitor.activateModification("target");


        try {
            Thread.sleep(5000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
