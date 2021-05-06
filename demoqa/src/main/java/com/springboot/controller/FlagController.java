package com.springboot.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

import com.springboot.interceptor.FlagControllerInterceptor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.abtasty.flagship.visitor.Visitor;

@RestController
public class FlagController {

    public Visitor visitor;

    @RequestMapping(method = RequestMethod.GET, value = "/flag/{flag_key}")
    public Object getFlag(HttpServletRequest request, @PathVariable String flag_key, @RequestParam String type, @RequestParam Boolean activate, @RequestParam String defaultValue) {

        Object flag = null;
        String error = "";
        Map<String, Object> obj = new HashMap<String, Object>();

        visitor = (Visitor) request.getAttribute("Visitor");
        switch (type) {

            case "bool":
                flag = visitor.getModification(flag_key, Boolean.parseBoolean(defaultValue), activate);
                break;
            case "string":
                flag = visitor.getModification(flag_key, defaultValue, activate);
                break;
            case "double":
                flag = visitor.getModification(flag_key, Double.parseDouble(defaultValue), activate);
                break;
            case "long":
                flag = visitor.getModification(flag_key, Long.parseLong(defaultValue), activate);
                break;
            case "int":
                flag = visitor.getModification(flag_key, Integer.parseInt(defaultValue), activate);
                break;
            case "float":
                flag = visitor.getModification(flag_key, Float.parseFloat(defaultValue), activate);
                break;

            case "JSONArray":
                try {
                    JSONArray array = new JSONArray(defaultValue.toString());
                    flag = visitor.getModification(flag_key, array, activate);
                } catch (Exception e) {
                    error = e.getMessage();
                }
                break;

            case "JSONObject":
                try {
                    JSONObject object = new JSONObject(defaultValue.toString());
                    flag = visitor.getModification(flag_key, object, activate);
                } catch (Exception e) {
                    error = e.getMessage();
                }
                break;
            default:
                error = "Type" + type + "not handled";
        }

        obj.put("value", flag != null ? flag.toString() : null);
        obj.put("error", error);
        System.out.println(obj);
        return obj;

    }

    @RequestMapping(method = RequestMethod.GET, value = "/flag/{flag_key}/info")
    public Object getFlagInfo(HttpServletRequest request, @PathVariable String flag_key) {

        JSONObject flagInfo = null;
        Map<String, Object> objInfo = new HashMap<String, Object>();
        Map flagInfoContent = new HashMap<String, Object>();
        visitor = (Visitor) request.getAttribute("Visitor");
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
        visitor = (Visitor) request.getAttribute("Visitor");
//        visitor.synchronizeModifications().get();
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
            case "double":
                visitor.updateContext(flag_key, Double.parseDouble(value));
                break;
            case "long":
                visitor.updateContext(flag_key, Long.parseLong(value));
                break;
            case "int":
                visitor.updateContext(flag_key, Integer.parseInt(value));
                break;
            case "float":
                visitor.updateContext(flag_key, Float.parseFloat(value));
                break;

        }
        visitor.synchronizeModifications().get();

        com.springboot.model.Visitor visitorAttribute = (com.springboot.model.Visitor) request.getSession().getAttribute(FlagControllerInterceptor.Vis);
        visitorAttribute.setContext(new HashMap<>(visitor.getContext()));
        request.getSession().setAttribute(FlagControllerInterceptor.Vis, visitorAttribute);

        return visitor.toString();
    }
}
