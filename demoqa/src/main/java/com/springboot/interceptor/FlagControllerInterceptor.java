package com.springboot.interceptor;

import java.util.concurrent.CountDownLatch;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.Visitor;

@Component
public class FlagControllerInterceptor implements HandlerInterceptor {

	private static final String Vis = "Visitor";
	public static Visitor visitor;
	private static Logger log = LoggerFactory.getLogger(FlagControllerInterceptor.class);
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		
		log.info("FlagInterceptor - prehandler BEGIN");
		
		final com.springboot.model.Visitor visAttribut = (com.springboot.model.Visitor) request.getSession().getAttribute(Vis);
	
		visitor = Flagship.newVisitor(visAttribut.getVisitor_id(), visAttribut.getContext());
		
		CountDownLatch latch = new CountDownLatch(1);
		
		visitor.updateContext("postcode", "31200", () -> {
		    System.out.println("Synchronized");
		    latch.countDown();
		});
		
		latch.await();
			
		//visitor.synchronizeModifications(null);
		
		request.setAttribute("Visitor", visitor);
		 
		 log.info("FlagInterceptor - prehandler ENDS");
		 
		return true;
	}
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		log.info("FlagInterceptor - posthandler");
	
		HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
	}
	
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		// TODO Auto-generated method stub
		log.info("FlagInterceptor - afterCompleting");
		HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
	}
}
