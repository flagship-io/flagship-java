package com.abtasty.demo;

import com.abtasty.flagship.hits.Screen;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.main.Visitor;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DemoFlagship {

    public static void main(String[] args)  {

        Flagship.start("bkk4s7gcmjcg07fke9dg", "Q6FDmj6F188nh75lhEato2MwoyXDS7y34VrAL4Aa", new FlagshipConfig());
//        Flagship.start("my_env_id", "my api key", new FlagshipConfig());

        Visitor visitor = Flagship.newVisitor("visitor1");
//
//        while (true) {
//            visitor.updateContext("isVIP", true);
        final long top = System.currentTimeMillis();
            visitor.synchronizeModifications().whenComplete((Void, error) -> {
                System.out.println("Timer = " + (System.currentTimeMillis() - top));
                Boolean displayFeature = visitor.getModification("vipFeature", false, false);
//                System.out.println("displayFeature = " + displayFeature);
//                visitor.activateModification("vipFeature");
            });
            visitor.sendHit(new Screen("main"));
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("END");
    }

    private static String getDate() {
        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss::SSS");
        Date resultDate = new Date(System.currentTimeMillis());
        return date_format.format(resultDate);
    }
}
