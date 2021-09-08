package com.abtasty.demo;

import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.utils.LogManager;
import com.abtasty.flagship.visitor.Visitor;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public class DemoFlagship {

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        CountDownLatch flagshipReadyLatch = new CountDownLatch(1);
        Flagship.start("bkk4s7gcmjcg07fke9dg", "Q6FDmj6F188nh75lhEato2MwoyXDS7y34VrAL4Aa",
                new FlagshipConfig.DecisionApi()
                        .withLogLevel(LogManager.Level.ALL)
                        .withStatusListener(newStatus -> {
                            System.out.println("NEW STATUS = " + newStatus.name());
                            if (newStatus.greaterThan(Flagship.Status.POLLING))
                                flagshipReadyLatch.countDown();
                        })
        );
        flagshipReadyLatch.await();
        Visitor visitor1 = Flagship.newVisitor("visitor_1")
                .context(new HashMap<String, Object>() {{
                    put("age", 32);
                }})
                .hasConsented(false)
                .build();
        visitor1.synchronizeModifications().get();

        Thread.sleep(3000);

        visitor1.synchronizeModifications().get();

        visitor1.setConsent(true);

        Thread.sleep(3000);
    }


}
