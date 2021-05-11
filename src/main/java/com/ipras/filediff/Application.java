package com.ipras.filediff;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.ipras.filediff.engine.BirtReportEngine;
import com.ipras.filediff.service.TextComparator;

public class Application {

	public static void main(String[] args) throws EncryptedDocumentException, InvalidFormatException, BirtException, IOException, URISyntaxException {

		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfig.class);

		TextComparator textComparator = context.getBean(TextComparator.class);
		BirtReportEngine reportEngine = context.getBean(BirtReportEngine.class);
		ReportDesignHandle design = textComparator.compareText();
		reportEngine.runReport(design);

		context.close();
		
	}

}