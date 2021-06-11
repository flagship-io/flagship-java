package com.springboot.controller;

import com.abtasty.flagship.visitor.Visitor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.springboot.controller.VisitorController.VisitorConstant;

@RestController
public class FlagController {

    @RequestMapping(method = RequestMethod.GET, value = "/flag/{flag_key}")
    public Object getFlag(HttpServletRequest request, @PathVariable String flag_key, @RequestParam String type, @RequestParam Boolean activate, @RequestParam String defaultValue) {

        Object flag = null;
        String error = "";
        Map<String, Object> obj = new HashMap<String, Object>();

        Visitor visitor = (Visitor) request.getSession().getAttribute(VisitorConstant);
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
                    JSONArray array = new JSONArray(defaultValue);
                    flag = visitor.getModification(flag_key, array, activate);
                } catch (Exception e) {
                    error = e.getMessage();
                }
                break;

            case "JSONObject":
                try {
                    JSONObject object = new JSONObject(defaultValue);
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
        return obj;

    }

    @RequestMapping(method = RequestMethod.GET, value = "/flag/{flag_key}/info")
    public Object getFlagInfo(HttpServletRequest request, @PathVariable String flag_key) {

        JSONObject flagInfo = null;
        Map<String, Object> objInfo = new HashMap<String, Object>();
        Map flagInfoContent = new HashMap<String, Object>();
        Visitor visitor = (Visitor) request.getSession().getAttribute(VisitorConstant);
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
        Visitor visitor = (Visitor) request.getSession().getAttribute(VisitorConstant);
        flagInfo = visitor.getModificationInfo(flag_key);

        if (flagInfo != null) {
            visitor.activateModification(flag_key);
            objInfo.put("activateValue", "Activation sent.");
        } else
            objInfo.put("activateValue", "Key not found, no activation sent.");
        return objInfo;
    }
}
