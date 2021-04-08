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

@RestController
public class VisitorController {

	private static final String Vis = "Visitor";
	private static com.springboot.model.Visitor currentVisitor = new com.springboot.model.Visitor("", null);
	public static Visitor visitor;
	
	@RequestMapping(method=RequestMethod.GET, value="/visitor")
	public ResponseEntity<com.springboot.model.Visitor> getEnvironement(final HttpSession session) {
		
        final com.springboot.model.Visitor visAttribut = (com.springboot.model.Visitor) session.getAttribute(Vis);
        
        if(visAttribut != null) {
        	System.out.println(visAttribut.toString());
        }
        
        return new ResponseEntity<com.springboot.model.Visitor>(currentVisitor, HttpStatus.OK);
	}
	
	@RequestMapping(method=RequestMethod.PUT, value="/visitor")
	public String setVisitor(@RequestBody com.springboot.model.Visitor vis, final HttpServletRequest request) throws InterruptedException {
		
		currentVisitor = vis;
		
		request.getSession().setAttribute(Vis, currentVisitor);
		
		visitor = Flagship.newVisitor(currentVisitor.getVisitor_id(), currentVisitor.getContext());

		CountDownLatch latch = new CountDownLatch(1);
		visitor.synchronizeModifications(new Visitor.OnSynchronizedListener() {
			@Override
			public void onSynchronized() {
				latch.countDown();
			}
		});
		latch.await();
//		request.getSession().setAttribute(Vis, new );
		return visitor.toString();
	}
}
