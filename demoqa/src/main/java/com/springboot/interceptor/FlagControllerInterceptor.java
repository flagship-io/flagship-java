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
		
		final com.springboot.model.Visitor visitorAttribut = (com.springboot.model.Visitor) request.getSession().getAttribute(Vis);
	
		visitor = Flagship.newVisitor(visitorAttribut.getVisitor_id(), visitorAttribut.getContext());
		
		CountDownLatch latch = new CountDownLatch(1);

		visitor.synchronizeModifications().whenComplete((instance, error)->{
			latch.countDown();
		});

		latch.await();

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

		log.info("FlagInterceptor - afterCompleting");
		HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
	}
}
