package com.abtasty.demo;

import com.abtasty.flagship.database.SQLiteCacheManager;
import com.abtasty.flagship.hits.Event;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.model.Flag;
import com.abtasty.flagship.model.FlagMetadata;
import com.abtasty.flagship.utils.LogManager;
import com.abtasty.flagship.visitor.Visitor;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import com.abtasty.flagship.hits.Screen;

public class DemoFlagship {

    public static void main(String[] args) throws InterruptedException, ExecutionException {

//        System.out.println("> " + System.getProperty("java.version"));
//        CountDownLatch flagshipReadyLatch = new CountDownLatch(1);
//        Flagship.start("_ENV_ID_", "_API_KEY_",
//                new FlagshipConfig.DecisionApi()
//                        .withLogLevel(LogManager.Level.ALL)
//                        .withStatusListener(newStatus -> {
//                            System.out.println("NEW STATUS = " + newStatus.name());
//                            if (newStatus.greaterThan(Flagship.Status.POLLING))
//                                flagshipReadyLatch.countDown();
//                        })
////                        .withCacheManager(new SQLiteCacheManager())
//        );
//
//        flagshipReadyLatch.await();
//        //
//        Visitor visitor = Flagship.newVisitor("visitor_id")
//                .context(new HashMap<String, Object>() {{
//                    put("my_context", true);
//                }}).build();
//        visitor.fetchFlags().get();
//        String value = visitor.getFlag("my_flag", "default").value(true);
//        System.out.println("My flag value is : " + value);
//        Thread.sleep(200);
//        visitor.sendHit(new Screen("DemoFlagship.java"));
//        visitor.sendHit(new Event(Event.EventCategory.USER_ENGAGEMENT, "action"));
//        Thread.sleep(200);
    }
}
