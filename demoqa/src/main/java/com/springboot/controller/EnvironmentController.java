package com.springboot.controller;

import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.utils.LogManager;
import com.springboot.model.Environment;
import com.springboot.service.LogHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.concurrent.TimeUnit;
import static com.springboot.controller.VisitorController.VisitorConstant;

@RestController
public class EnvironmentController {

    private static final String EnvironmentConstant = "Environment";

    @RequestMapping(method = RequestMethod.GET, value = "/env")
    public ResponseEntity<Environment> getEnvironment(final HttpSession session) {

        final Environment environmentAttribute = (Environment) session.getAttribute(EnvironmentConstant);
        return new ResponseEntity<Environment>(environmentAttribute, HttpStatus.OK);
    }

    private TimeUnit getTimeUnit(String unit) {

        if (unit != null) {
            switch (unit) {
                case "milliseconds":
                    return TimeUnit.MILLISECONDS;
                case "seconds":
                    return TimeUnit.SECONDS;
                case "minutes":
                    return TimeUnit.MINUTES;
            }
        }
        return TimeUnit.MILLISECONDS;


    }

    private FlagshipConfig<?> getFlagshipConfig(Environment environmentModel) {
        if (environmentModel.getFlagship_mode().equals("api")) {
            return new FlagshipConfig.DecisionApi()
                    .withLogLevel(LogManager.Level.ALL)
                    .withTimeout(environmentModel.getTimeout())
                    .withLogManager(new LogManager() {
                        @Override
                        public void onLog(Level level, String tag, String message) {
                            LogHelper.appendToLogFile(level, tag, message);
                        }
                    });

        } else {
            return new FlagshipConfig.Bucketing()
                    .withLogLevel(LogManager.Level.ALL)
                    .withTimeout(environmentModel.getTimeout())
                    .withLogManager(new LogManager() {
                        @Override
                        public void onLog(Level level, String tag, String message) {
                            LogHelper.appendToLogFile(level, tag, message);
                        }
                    })
                    .withPollingIntervals(environmentModel.getPolling_interval(), getTimeUnit(environmentModel.getPolling_interval_unit()));
        }
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/env")
    public Environment setEnvironment(@RequestBody Environment environmentModel, final HttpServletRequest request) {

        request.getSession().setAttribute(EnvironmentConstant, environmentModel);
        request.getSession().setAttribute(VisitorConstant, null);
        LogHelper.clearLogFile();
        Flagship.start(environmentModel.getEnvironment_id(), environmentModel.getApi_key(), getFlagshipConfig(environmentModel));
        return environmentModel;
    }
}
