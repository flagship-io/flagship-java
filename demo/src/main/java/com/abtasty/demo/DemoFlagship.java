package com.abtasty.demo;

import com.abtasty.flagship.hits.Event;
import com.abtasty.flagship.hits.Page;
import com.abtasty.flagship.hits.Screen;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.main.Visitor;
import com.abtasty.flagship.model.Modification;
import com.abtasty.flagship.utils.ILogManager;

import java.util.HashMap;
import java.util.logging.Level;

public class DemoFlagship {

    public static void main(String[] args) {


        class CustomLogManager extends ILogManager {

            public CustomLogManager(LogMode mode) {
                super(mode);
            }

            @Override
            public void onLog(Level level, String tag, String message) {
                if (isLogApplyToLogMode(level)) {
                    System.out.println("FLAGSHIP => " + tag + " " + message);
                }
            }
        }

//        Flagship.start("my env id", "my api key", new FlagshipConfig());
        Flagship.start("bkk4s7gcmjcg07fke9dg", "Q6FDmj6F188nh75lhEato2MwoyXDS7y34VrAL4Aa", new FlagshipConfig().withLogManager(new CustomLogManager(ILogManager.LogMode.ALL)));
        Visitor visitor = Flagship.newVisitor("toto");
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
//        hashMap.put("Age", new FlagshipConfig());
        hashMap.put("Age", 31);
        hashMap.put("isVIPUser", true);
        hashMap.put("daysSinceLastLaunch", 2);
        visitor.updateContext(hashMap);
        visitor.updateContext("title", "Grand Mamamouchi", () -> {
            System.out.println("Update context synchronized");
        });
        visitor.synchronizeModifications(() -> {
            String value2 = visitor.getModification("isref", "default", false);
            visitor.activateModification("isref");
            int value3 = visitor.getModification("release", 0, true);
            String value4 = visitor.getModification("title", "default", true);
            int value5 = visitor.getModification("all_users", 0, true);
            Modification m = new Modification("", "", "", "", true, null);
            boolean value6 = visitor.getModification("featureEnabled", false, true);

            System.out.println("Value 2 = " + value2.toString()); //
            System.out.println("Value 3 = " + value3); //
            System.out.println("Value 4 = " + value4.toString()); //
            System.out.println("Value 5 = " + value5);
            System.out.println("Value 6 = " + value6); //

            visitor.sendHit(new Page("https://mydomain.com/java").withResolution(100, 100));
            visitor.sendHit(new Screen("Page Java").withResolution(100, 100));
            visitor.sendHit(new Event(Event.EventCategory.USER_ENGAGEMENT, "coucou").withResolution(2, 2).withEventLabel("label").withEventValue(666).withLocale("fr_FR"));
        });
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        Page p = new Page("www.java.com/version1");
    }
}
