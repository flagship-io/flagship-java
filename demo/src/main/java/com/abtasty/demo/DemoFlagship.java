package com.abtasty.demo;

import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.main.visitor.Visitor;
import com.abtasty.flagship.utils.LogManager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


public class DemoFlagship {

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        CountDownLatch flagshipReadyLatch = new CountDownLatch(1);

        Flagship.start("bkk4s7gcmjcg07fke9dg", "Q6FDmj6F188nh75lhEato2MwoyXDS7y34VrAL4Aa",
                new FlagshipConfig()
                        .withLogLevel(LogManager.Level.ALL)
                        .withFlagshipMode(Flagship.Mode.BUCKETING)
                        .withBucketingPollingIntervals(5, TimeUnit.SECONDS)
                        .withStatusListener(newStatus -> {
                            System.out.println("NEW STATUS = " + newStatus.name());
                            if (newStatus == Flagship.Status.READY)
                                flagshipReadyLatch.countDown();
                        })
        );
        flagshipReadyLatch.await();

        Visitor visitor1 = Flagship.newVisitor("toto");
        visitor1.synchronizeModifications().get();

        CountDownLatch flagshipReadyLatch2 = new CountDownLatch(1);
        Flagship.start("bkk4s7gcmjcg07fke9dg", "Q6FDmj6F188nh75lhEato2MwoyXDS7y34VrAL4Aa",
                new FlagshipConfig()
                        .withLogLevel(LogManager.Level.ALL)
                        .withFlagshipMode(Flagship.Mode.DECISION_API)
                        .withBucketingPollingIntervals(5, TimeUnit.SECONDS)
                        .withStatusListener(newStatus -> {
                            System.out.println("NEW STATUS = " + newStatus.name());
                            if (newStatus == Flagship.Status.READY)
                                flagshipReadyLatch2.countDown();
                        })
        );

        visitor1.synchronizeModifications().get();
//        Visitor visitor2 = Flagship.newVisitor("toto2");
//        visitor2.synchronizeModifications().get();
    }


}
