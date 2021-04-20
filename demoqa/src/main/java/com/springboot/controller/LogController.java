package com.springboot.controller;

import com.springboot.service.LogHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
public class LogController {
	
	@RequestMapping(method=RequestMethod.GET, value="/logs")
	public ResponseEntity<String> getLogs(final HttpSession session) {

		String content = LogHelper.getLogFileContent();
        return new ResponseEntity<String>(content, HttpStatus.OK);
	}

	@RequestMapping(method=RequestMethod.GET, value="/clear")
	public ResponseEntity<String> clearLogs(final HttpSession session) {

		LogHelper.clearLogFile();

		String content = LogHelper.getLogFileContent();
		return new ResponseEntity<String>(content, HttpStatus.OK);
	}
}
