package com.springboot.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.visitor.Visitor;

import java.util.concurrent.ExecutionException;

@RestController
public class VisitorController {

    public static final String VisitorConstant = "Visitor";

//    @RequestMapping(method = RequestMethod.GET, value = "/visitor")
//    public ResponseEntity<com.springboot.model.Visitor> getVisitor(final HttpSession session) {
//
//        final com.springboot.model.Visitor visitorAttribute = (com.springboot.model.Visitor) session.getAttribute(VisitorConstant);
//
//        return new ResponseEntity<com.springboot.model.Visitor>(visitorAttribute, HttpStatus.OK);
//    }

    @RequestMapping(method = RequestMethod.GET, value = "/visitor")
    public String getVisitor(HttpServletRequest request) {

        Visitor visitor = (Visitor) request.getSession().getAttribute(VisitorConstant);
        return (visitor != null) ? visitor.toString() : "";
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/visitor")
    public String setVisitor(@RequestBody com.springboot.model.Visitor visitorModel, final HttpServletRequest request) throws InterruptedException, ExecutionException {
        Visitor visitor = Flagship.newVisitor(visitorModel.getVisitor_id(), visitorModel.getAuthenticated(), visitorModel.getContext());
        visitor.setConsent(visitorModel.isConsent());
        visitor.synchronizeModifications().get();
        request.getSession().setAttribute(VisitorConstant, visitor);
        return visitor.toString();
    }


    @RequestMapping(method = RequestMethod.PUT, value = "/visitor/context")
    public String updateContext(@RequestBody com.springboot.model.Context context, HttpServletRequest request) throws ExecutionException, InterruptedException {

        Visitor visitor = (Visitor) request.getSession().getAttribute(VisitorConstant);
        String key = context.getKey();
        String value = context.getValue();
        switch (context.getType()) {

            case "bool":
                visitor.updateContext(key, Boolean.parseBoolean(value));
                break;
            case "string":
                visitor.updateContext(key, value);
                break;
            case "double":
                visitor.updateContext(key, Double.parseDouble(value));
                break;
            case "long":
                visitor.updateContext(key, Long.parseLong(value));
                break;
            case "int":
                visitor.updateContext(key, Integer.parseInt(value));
                break;
            case "float":
                visitor.updateContext(key, Float.parseFloat(value));
                break;

        }
        visitor.synchronizeModifications().get();
        request.getSession().setAttribute(VisitorConstant, visitor);
        return visitor.toString();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/visitor/authenticate")
    public String authenticate(HttpServletRequest request, @RequestParam String newVisitorId) throws ExecutionException, InterruptedException {
        Visitor visitor = (Visitor) request.getSession().getAttribute(VisitorConstant);
        visitor.authenticate(newVisitorId);
        visitor.synchronizeModifications().get();
        request.getSession().setAttribute(VisitorConstant, visitor);
        return visitor.toString();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/visitor/unauthenticate")
    public String unauthenticate(HttpServletRequest request) throws ExecutionException, InterruptedException {

        Visitor visitor =  (Visitor) request.getSession().getAttribute(VisitorConstant);
        visitor.unauthenticate();
        visitor.synchronizeModifications().get();
        request.getSession().setAttribute(VisitorConstant, visitor);
        return visitor.toString();
    }
}
