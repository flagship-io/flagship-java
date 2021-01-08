
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.main.Visitor;

import java.util.HashMap;

public class Flagshipapp {

    public static void main(String[] args) {
//        Flagship.start("my env id", "my api key", new FlagshipConfig());
        Flagship.start("bkk4s7gcmjcg07fke9dg", "Q6FDmj6F188nh75lhEato2MwoyXDS7y34VrAL4Aa", new FlagshipConfig());
        Visitor visitor = Flagship.newVisitor("toto");
        HashMap<String, Object> hashMap = new HashMap();
//        hashMap.put("Age", new FlagshipConfig());
        hashMap.put("Age", 31);
        visitor.updateContext(hashMap);
        visitor.synchronizeModifications();
        System.out.println("Here cap : " + visitor.toString());

    }
}