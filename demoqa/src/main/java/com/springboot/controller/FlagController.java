package com.springboot.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.abtasty.flagship.main.Visitor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class FlagController {
	
	public Visitor vis;

	@RequestMapping(method=RequestMethod.GET, value="/flag/{flag_key}" )
	public Object getFlag(HttpServletRequest request, @PathVariable String flag_key, @RequestParam String type, @RequestParam Boolean activate, @RequestParam String defaultValue) {
		
		Object flag = null;
		String error = "";
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> obj = new HashMap<String,Object>();
		
		vis = (Visitor) request.getAttribute("Visitor");
		switch(type) {

			case "bool":
				flag = vis.getModification(flag_key, Boolean.parseBoolean(defaultValue), activate);
				break;

			case "string":
				flag = vis.getModification(flag_key, defaultValue, activate);
				break;

			case "number":
				flag = vis.getModification(flag_key, Double.parseDouble(defaultValue), activate);
				break;

			case "array":
				try {

					List<Object> arrayVal = mapper.readValue(defaultValue, new TypeReference<List<Object>>() {});
					flag = vis.getModification(flag_key, arrayVal, activate);
					System.out.println(arrayVal.toString());

				}catch(Exception e) {
					error = e.getMessage();
				}
				break;

			case "object":
				try {

					Object objVal = mapper.readValue(defaultValue, new TypeReference<Object>() {});
					flag = vis.getModification(flag_key, objVal, activate);

				}catch(Exception e) {
					error = e.getMessage();
				}
				break;
			default:
				error = "Type"+ type + "not handled";
		}
		
		if(error != "") {
			error = "there is an error";
		}
		
		obj.put("value", flag);
		obj.put("error", error);
		System.out.println(obj);
		return obj;
		
	}

	@RequestMapping(method=RequestMethod.GET, value="/flag/{flag_key}/info")
	public Object getFlagInfo(HttpServletRequest request, @PathVariable String flag_key){
		
		Object flagInfo = null;
		Map<String, Object> objInfo = new HashMap<String, Object>();
		flagInfo = vis.getModificationInfo(flag_key);
		System.out.println(flagInfo);
		objInfo.put("value", flagInfo.toString());
		
		return objInfo;
		
	}
}
