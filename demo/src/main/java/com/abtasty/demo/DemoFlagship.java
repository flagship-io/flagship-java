package com.abtasty.demo;

import com.abtasty.flagship.hits.Screen;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.main.Visitor;

public class DemoFlagship {

    public static void main(String[] args) {


        Flagship.start("my_env_id", "my api key", new FlagshipConfig());

        Visitor visitor = Flagship.newVisitor("visitor1");

        visitor.updateContext("isVIP", true);

        visitor.synchronizeModifications(() -> {
            Boolean displayFeature = visitor.getModification("vipFeature", false, false);
            System.out.println("displayFeature = " + displayFeature);
            visitor.activateModification("vipFeature");
        });
        visitor.sendHit(new Screen("main"));

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
