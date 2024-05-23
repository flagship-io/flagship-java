package com.abtasty.springboot;

import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.utils.LogManager;
import com.abtasty.flagship.visitor.Visitor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

@SpringBootApplication
public class Application {

    public Application() {

        Flagship.start("******", "********",
                new FlagshipConfig.DecisionApi()
                        .withLogLevel(LogManager.Level.ALL)
                        .withStatusListener(newStatus -> {
                            System.out.println("NEW STATUS = " + newStatus.name());
                        })
        );
        Visitor visitor = Flagship.newVisitor("anonymous1")
                .context(new HashMap<String, Object>() {{
                    put("my_context", true);
                }}).build();
        visitor.authenticate("logged1");
        try {
            visitor = visitor.fetchFlags().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
