package com.springboot.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


import com.springboot.service.LogHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.springframework.web.bind.annotation.RestController;

import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.utils.LogManager;
import com.springboot.model.Environment;

import java.util.concurrent.TimeUnit;

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
                case "nanoseconds":
                    return TimeUnit.NANOSECONDS;

                case "microseconds":
                    return TimeUnit.MICROSECONDS;

                case "milliseconds":
                    return TimeUnit.MILLISECONDS;

                case "seconds":
                    return TimeUnit.SECONDS;

                case "minutes":
                    return TimeUnit.MINUTES;

                case "hours":
                    return TimeUnit.HOURS;

                case "days":
                    return TimeUnit.DAYS;
            }
        }
        return TimeUnit.MILLISECONDS;


    }

    @RequestMapping(method = RequestMethod.PUT, value = "/env")
    public Environment setEnvironment(@RequestBody Environment environmentModel, final HttpServletRequest request) {

        request.getSession().setAttribute(EnvironmentConstant, environmentModel);

        LogHelper.clearLogFile();

        Flagship.start(environmentModel.getEnvironment_id(), environmentModel.getApi_key(), new FlagshipConfig()
                .withFlagshipMode(environmentModel.getFlagship_mode().equals("api") ? Flagship.Mode.DECISION_API : Flagship.Mode.BUCKETING)
                .withBucketingPollingIntervals(environmentModel.getPolling_interval(), getTimeUnit(environmentModel.getPolling_interval_unit()))
                .withLogLevel(LogManager.Level.ALL)
                .withTimeout(environmentModel.getTimeout())
                .withLogManager(new LogManager() {
                    @Override
                    public void onLog(Level level, String tag, String message) {
                        LogHelper.appendToLogFile(level, tag, message);
                    }
                }));

        return environmentModel;

    }
}
