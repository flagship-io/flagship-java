package com.springboot.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.abtasty.flagship.main.Visitor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class FlagController {

    public Visitor visitor;

    @RequestMapping(method = RequestMethod.GET, value = "/flag/{flag_key}")
    public Object getFlag(HttpServletRequest request, @PathVariable String flag_key, @RequestParam String type, @RequestParam Boolean activate, @RequestParam String defaultValue) {

        Object flag = null;
        String error = "";
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> obj = new HashMap<String, Object>();

        visitor = (Visitor) request.getAttribute("Visitor");
        switch (type) {

            case "bool":
                flag = visitor.getModification(flag_key, Boolean.parseBoolean(defaultValue), activate);
                break;

            case "string":
                flag = visitor.getModification(flag_key, defaultValue, activate);
                break;

            case "number":
                flag = visitor.getModification(flag_key, Double.parseDouble(defaultValue), activate);
                break;

            case "array":
                try {

                    List<Object> arrayVal = mapper.readValue(defaultValue, new TypeReference<List<Object>>() {
                    });
                    flag = visitor.getModification(flag_key, arrayVal, activate);
                    System.out.println(arrayVal.toString());

                } catch (Exception e) {
                    error = e.getMessage();
                }
                break;

            case "object":
                try {

                    Object objVal = mapper.readValue(defaultValue, new TypeReference<Object>() {
                    });
                    flag = visitor.getModification(flag_key, objVal, activate);

                } catch (Exception e) {
                    error = e.getMessage();
                }
                break;
            default:
                error = "Type" + type + "not handled";
        }

        if (error != "") {
            error = "there is an error";
        }

        obj.put("value", flag);
        obj.put("error", error);
        System.out.println(obj);
        return obj;

    }

    @RequestMapping(method = RequestMethod.GET, value = "/flag/{flag_key}/info")
    public Object getFlagInfo(HttpServletRequest request, @PathVariable String flag_key) {

        JSONObject flagInfo = null;
        Map<String, Object> objInfo = new HashMap<String, Object>();
        Map flagInfoContent = new HashMap<String, Object>();
        flagInfo = visitor.getModificationInfo(flag_key);

        if (flagInfo == null) {
            objInfo.put("value", "Key doesn't exist");
        } else {
            flagInfoContent = flagInfo.toMap();
            objInfo.put("value", flagInfoContent);
        }

        return objInfo;

    }

    @RequestMapping(method = RequestMethod.GET, value = "/flag/{flag_key}/activate")
    public Object getFlagModification(HttpServletRequest request, @PathVariable String flag_key) throws ExecutionException, InterruptedException {

        JSONObject flagInfo = null;
        Map<String, Object> objInfo = new HashMap<String, Object>();
        visitor.synchronizeModifications().get();
        flagInfo = visitor.getModificationInfo(flag_key);

        if (flagInfo != null) {

            visitor.activateModification(flag_key);
            objInfo.put("activateValue", "Activation sent.");

        } else {

            objInfo.put("activateValue", "Key not found, no activation sent.");

        }

        return objInfo;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/flag/{flag_key}/updateContext")
    public String getFlagUpdateContext(HttpServletRequest request, @PathVariable String flag_key, @RequestParam String type, @RequestParam String value) throws ExecutionException, InterruptedException {

        visitor = (Visitor) request.getAttribute("Visitor");

        switch (type) {

            case "bool":
                visitor.updateContext(flag_key, Boolean.parseBoolean(value));
                break;

            case "string":
                visitor.updateContext(flag_key, value);
                break;

            case "number":
                visitor.updateContext(flag_key, Double.parseDouble(value));
                break;

        }
        visitor.synchronizeModifications().get();
        return visitor.toString();

    }
}
