package com.springboot.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.springboot.interceptor.FlagControllerInterceptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.visitor.Visitor;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

@RestController
public class VisitorController {

    private static final String VisitorConstant = "Visitor";
    public static Visitor visitor;

    public static final String VisitorAttribute = "FsVisitor";

    @RequestMapping(method = RequestMethod.GET, value = "/visitor")
    public ResponseEntity<com.springboot.model.Visitor> getEnvironment(final HttpSession session) {

        final com.springboot.model.Visitor visitorAttribute = (com.springboot.model.Visitor) session.getAttribute(VisitorConstant);

        return new ResponseEntity<com.springboot.model.Visitor>(visitorAttribute, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/visitor")
    public String setVisitor(@RequestBody com.springboot.model.Visitor visitorModel, final HttpServletRequest request) throws InterruptedException, ExecutionException {
//
//        request.getSession().setAttribute(VisitorConstant, visitorModel);
//
//        visitor = Flagship.newVisitor(visitorModel.getVisitor_id(), visitorModel.getAuthenticated(), visitorModel.getContext());
//
//        visitor.synchronizeModifications().get();
//
//        request.getSession().setAttribute(VisitorAttribute, visitor);
//        return visitor.toString();

        Visitor visitor = Flagship.newVisitor(visitorModel.getVisitor_id(), visitorModel.getAuthenticated(), visitorModel.getContext());
        request.getSession().setAttribute(VisitorAttribute, visitor);
        return visitor.toString();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/authenticate")
    public String authenticate(HttpServletRequest request, @RequestParam String newVisitorId) throws ExecutionException, InterruptedException {

//        final com.springboot.model.Visitor visitorAttribute = (com.springboot.model.Visitor) request.getSession().getAttribute(VisitorConstant);
//        visitor = Flagship.newVisitor(visitorAttribute.getVisitor_id(), visitorAttribute.getAuthenticated(), visitorAttribute.getContext());
//        visitor.authenticate(newVisitorId);
//        visitor.synchronizeModifications().get();
//        visitorAttribute.setVisitor_id(newVisitorId);
//        visitorAttribute.setAuthenticated(true);
//        request.getSession().setAttribute(FlagControllerInterceptor.Vis, visitorAttribute);
//        request.getSession().setAttribute(VisitorAttribute, visitor);
//        return visitor.toString();

        Visitor visitor = (Visitor) request.getSession().getAttribute(VisitorAttribute);
        visitor.authenticate(newVisitorId);
        visitor.synchronizeModifications().get();
        request.getSession().setAttribute(VisitorAttribute, visitor);
        return visitor.toString();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/unauthenticate")
    public String unauthenticate(HttpServletRequest request) throws ExecutionException, InterruptedException {

        Visitor visitor =  (Visitor) request.getSession().getAttribute(VisitorAttribute);
        visitor.unauthenticate();
        visitor.synchronizeModifications().get();
        request.getSession().setAttribute(VisitorAttribute, visitor);
        return visitor.toString();
    }
}
