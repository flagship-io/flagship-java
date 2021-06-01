package com.abtasty.demo;

import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.utils.FlagshipContext;
import com.abtasty.flagship.utils.LogManager;
import com.abtasty.flagship.visitor.Visitor;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class DemoFlagship {

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        Flagship.start("envId", "apiKey", new FlagshipConfig.Bucketing()
                .withTimeout(3)
                .withPollingIntervals(3, TimeUnit.SECONDS)
                .withLogLevel(LogManager.Level.ALL));


        Visitor visitor1 = Flagship.newVisitor("toto");

        visitor1.synchronizeModifications().get();

        CountDownLatch flagshipReadyLatch = new CountDownLatch(1);
        Flagship.start("bkk4s7gcmjcg07fke9dg", "Q6FDmj6F188nh75lhEato2MwoyXDS7y34VrAL4Aa",
                new FlagshipConfig.Bucketing()
                        .withLogLevel(LogManager.Level.ALL)
                        .withPollingIntervals(0, TimeUnit.SECONDS)
                        .withStatusListener(newStatus -> {
                            System.out.println("NEW STATUS = " + newStatus.name());
                            if (newStatus == Flagship.Status.READY)
                                flagshipReadyLatch.countDown();
                        })
        );
        flagshipReadyLatch.await();

        visitor1.updateContext("fs_client", "pas java");
        visitor1.updateContext(FlagshipContext.LOCATION_LAT, 3.2);

//        Visitor visitor1 = Flagship.newVisitor("toto");
        visitor1.updateContext("coucou", 1);
        visitor1.synchronizeModifications().get();
        visitor1.activateModification("activate");
        visitor1.setConsent(false);
        visitor1.synchronizeModifications().get();
        visitor1.activateModification("activate2");

        Thread.sleep(10000);
    }
}
