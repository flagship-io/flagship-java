package com.springboot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.springboot.interceptor.FlagControllerInterceptor;
import com.springboot.interceptor.HitControllerInterceptor;

@Component
public class WebInterceptorConfig implements WebMvcConfigurer {
	
	@Autowired
	FlagControllerInterceptor flagInter;
	
	@Autowired
	HitControllerInterceptor hitInter;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(flagInter).addPathPatterns("/flag/{flag_key}");
		registry.addInterceptor(hitInter).addPathPatterns("/hit");
	}
}
