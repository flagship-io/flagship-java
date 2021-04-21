package com.springboot.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.Visitor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

@RestController
public class VisitorController {

    private static final String VisitorConstant = "Visitor";
    public static Visitor visitor;

    @RequestMapping(method = RequestMethod.GET, value = "/visitor")
    public ResponseEntity<com.springboot.model.Visitor> getEnvironment(final HttpSession session) {

        final com.springboot.model.Visitor visitorAttribute = (com.springboot.model.Visitor) session.getAttribute(VisitorConstant);

        return new ResponseEntity<com.springboot.model.Visitor>(visitorAttribute, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/visitor")
    public String setVisitor(@RequestBody com.springboot.model.Visitor visitorModel, final HttpServletRequest request) throws InterruptedException, ExecutionException {

        request.getSession().setAttribute(VisitorConstant, visitorModel);

        visitor = Flagship.newVisitor(visitorModel.getVisitor_id(), visitorModel.getContext());

        visitor.synchronizeModifications().get();

        return visitor.toString();
    }
}
