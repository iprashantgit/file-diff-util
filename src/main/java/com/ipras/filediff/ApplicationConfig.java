package com.ipras.filediff;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.ipras.filediff.design.ReportDesigner;
import com.ipras.filediff.engine.BirtReportEngine;
import com.ipras.filediff.service.TextComparator;

@Configuration
@PropertySource("file:application.properties")
public class ApplicationConfig {
	
	@Bean
	public TextComparator textComparator() {
		return new TextComparator();
	}
	
	@Bean
	public ReportDesigner reportDesigner() {
		return new ReportDesigner();
	}
	
	@Bean
	public BirtReportEngine birtReportEngine() {
		return new BirtReportEngine();
	}
}
