package com.abtasty.demo;

import com.abtasty.flagship.hits.Page;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.utils.FlagshipContext;
import com.abtasty.flagship.utils.LogManager;
import com.abtasty.flagship.visitor.Visitor;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class DemoFlagship {

    public static void main(String[] args) throws InterruptedException, ExecutionException {

//        Visitor visitor1 = Flagship.newVisitor("toto", false, new HashMap<String, Object>() {{ put("age", 32);}});
//
//        visitor1.synchronizeModifications().get();
//
        CountDownLatch flagshipReadyLatch = new CountDownLatch(1);
        Flagship.start("bkk4s7gcmjcg07fke9dg", "Q6FDmj6F188nh75lhEato2MwoyXDS7y34VrAL4Aa",
                new FlagshipConfig.Bucketing()
//                new FlagshipConfig.DecisionApi()
                        .withLogLevel(LogManager.Level.ALL)
                        .withPollingIntervals(0, TimeUnit.SECONDS)
                        .withStatusListener(newStatus -> {
                            System.out.println("NEW STATUS = " + newStatus.name());
                            if (newStatus.greaterThan(Flagship.Status.POLLING))
                                flagshipReadyLatch.countDown();
                        })
        );
        flagshipReadyLatch.await();
        Visitor visitor1 = Flagship.newVisitor("taze", false, new HashMap<String, Object>() {{ put("age", 32);}});
        Visitor visitor2 = Flagship.newVisitor("toto2", false, new HashMap<String, Object>() {{ put("age", 32);}});
        visitor1.synchronizeModifications().get();
        JSONObject array = visitor1.getModification("json", new JSONObject(), true);
//        System.out.println(visitor1.getModification("all_users", 0, true));
        System.out.println("Json : " + array);
        System.out.println("String : " +  visitor1.getModification("string", null, true));
        Thread.sleep(10000);
    }


}
