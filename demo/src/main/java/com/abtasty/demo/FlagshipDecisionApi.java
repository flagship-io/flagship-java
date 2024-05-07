package com.abtasty.demo;

import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.model.Flag;
import com.abtasty.flagship.model.FlagMetadata;
import com.abtasty.flagship.utils.LogManager;
import com.abtasty.flagship.visitor.Visitor;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class FlagshipDecisionApi {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Long startMilliSecs = System.currentTimeMillis();
        Flagship.start("******", "******", new FlagshipConfig.DecisionApi() // Will start the SDK with Api mode.
//				.withLogManager(new CustomLogManager())
                .withLogLevel(LogManager.Level.ALL)
                .withStatusListener(newStatus -> {
                    if (newStatus == Flagship.Status.READY)
                        System.out.println("SDK is ready to use.");
                })
                //.withPollingIntervals(20, TimeUnit.SECONDS)
                .withTimeout(200));
        Flagship.Status status = Flagship.getStatus();
        System.out.println("Status : " + status);
        System.out.println("time taken by flagship api ----> " + (System.currentTimeMillis() - startMilliSecs));
        Visitor visitor = Flagship.newVisitor("79247395982347")
                .context(new HashMap<String, Object>() {
                    {
                        put("Product Codes", "GCOMREADER");
                    }
                })
                .build();
        HashMap<String, Object> visitorContext = visitor.getContext();
        System.out.println("Visitor Context : " + visitorContext);

        /** Handle Asynchronous execution: Do not block, code will be executed when fetch flag is done **/
        visitor.fetchFlags().whenComplete((instance, error) -> { // Asynchronous non blocking call
            // flag synchronization has been completed.
            Flag<String> readerFlag = visitor.getFlag("ReaderType", "none");
            System.out.println("flag value 1 --> " + readerFlag.value(false));
            System.out.println("flag metadata 1 --> " + readerFlag.metadata().toJSON());
        });

//        /** Handle Synchronous execution: Block until fetch flag is done. **/
//        visitor.fetchFlags().get();

        //String readerFlag = visitor.getModification("ReaderType", "none"); --> DEPRECATED IN 3.0.X
        Flag<String> readerFlag = visitor.getFlag("ReaderType", "none");
        System.out.println("flag value 2 --> " + readerFlag.value(false));
        FlagMetadata metadata = visitor.
                getFlag("ReaderType", "none").metadata();
        System.out.println("flag metadata 2 --> " + metadata.toJSON());

        Thread.sleep(2000);
    }
}
