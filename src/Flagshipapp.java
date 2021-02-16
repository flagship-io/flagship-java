
import com.abtasty.flagship.hits.Event;
import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.hits.Page;
import com.abtasty.flagship.hits.Screen;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.main.Visitor;

import java.util.HashMap;

public class Flagshipapp {

    public static void main(String[] args) {
//        Flagship.start("my env id", "my api key", new FlagshipConfig());
        Flagship.start("bkk4s7gcmjcg07fke9dg", "Q6FDmj6F188nh75lhEato2MwoyXDS7y34VrAL4Aa", new FlagshipConfig());
        Visitor visitor = Flagship.newVisitor("toto");
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
//        hashMap.put("Age", new FlagshipConfig());
        hashMap.put("Age", 31);
        hashMap.put("isVIPUser", false);
        visitor.updateContext(hashMap);
        visitor.updateContext("title", "Grand Mamamouchi", () -> {
            System.out.println("Update context synchronized");
        });
        visitor.synchronizeModifications(() -> {
            String value = visitor.getModification("isref", "coucou");
            System.out.println("value => " + value);
            visitor.activateModification("isref");
            visitor.sendHit(new Page("https://mydomain.com/java").withResolution(100, 100));
            visitor.sendHit(new Screen("Page Java").withResolution(100, 100));
            visitor.sendHit(new Event(Event.EventCategory.USER_ENGAGEMENT, "coucou").withResolution(2,2).withEventLabel("label").withEventValue(666).withLocale("fr_FR"));
        });
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        Page p = new Page("www.java.com/version1");

    }
}