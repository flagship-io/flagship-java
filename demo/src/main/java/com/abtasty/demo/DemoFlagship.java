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
        Flagship.start("_", "__",
                new FlagshipConfig.DecisionApi()
                        .withLogLevel(LogManager.Level.ALL)
                        .withStatusListener(newStatus -> {
                            System.out.println("NEW STATUS = " + newStatus.name());
                            if (newStatus.greaterThan(Flagship.Status.POLLING))
                                flagshipReadyLatch.countDown();
                        })
        );
        flagshipReadyLatch.await();

        Visitor visitor1 = Flagship.newVisitor("visitor_1", Visitor.Instance.SINGLE_INSTANCE)
                .build();

        Visitor visitor2 = Flagship.newVisitor("visitor_2", Visitor.Instance.SINGLE_INSTANCE)
                .build();

        visitor1.updateContext("color", "blue");

        Flagship.getVisitor().updateContext("color", "red");

        System.out.println("=> " + (visitor1.getContext().get("color") == "blue"));
        System.out.println("=> " + (visitor2.getContext().get("color") == "red"));
        System.out.println("=> " + (Flagship.getVisitor().getContext().get("color") == "red"));

        Thread.sleep(10000);
    }


}
