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
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.utils.LogManager;
import com.springboot.model.Environment;

@RestController
public class EnvironmentController {
	
	private static final String Env = "Environment";
	private static Environment currentEnv = new Environment("","", 0, 0);
	
	@RequestMapping(method=RequestMethod.GET, value="/env")
	public ResponseEntity<Environment> getEnvironment(final HttpSession session) {
		  
        final Environment envAttribut = (Environment) session.getAttribute(Env);
        
        if(envAttribut != null) {
        	System.out.println(envAttribut.toString());
        }
        
        return new ResponseEntity<Environment>(currentEnv, HttpStatus.OK);
	}
	
	@RequestMapping(method=RequestMethod.PUT, value="/env")
	public Environment setEnvironment(@RequestBody Environment env, final HttpServletRequest request) {
		
		currentEnv = env;
		
		request.getSession().setAttribute(Env, currentEnv);
		
		//Flagship.start(currentEnv.getEnvironment_id(), currentEnv.getApi_key());
		
		Flagship.start(currentEnv.getEnvironment_id(), currentEnv.getApi_key(), new FlagshipConfig()
	              .withFlagshipMode(Flagship.Mode.DECISION_API)
	              .withLogMode(LogManager.LogMode.ALL)
	              .withTimeout(currentEnv.getTimeout()));
		
		return currentEnv;
		
	}
}
