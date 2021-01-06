
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.main.Visitor;

import java.util.HashMap;

public class Flagshipapp {

    public static void main(String[] args) {
        Flagship.start("my env id", "my api key", new FlagshipConfig());
        Visitor visitor = Flagship.newVisitor("toto");
        HashMap<String, Object> hashMap = new HashMap();
        hashMap.put("Age", new FlagshipConfig());
        visitor.updateContext(hashMap);
        System.out.println("Here cap : " + visitor.toString());

    }
}