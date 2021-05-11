package com.ipras.filediff.design;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.model.api.DesignConfig;
import org.eclipse.birt.report.model.api.IDesignEngine;
import org.eclipse.birt.report.model.api.IDesignEngineFactory;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.SessionHandle;
import org.eclipse.birt.report.model.api.css.CssStyleSheetHandle;

import com.ibm.icu.util.ULocale;

public class ReportDesigner {

	public ReportDesignHandle buildReport(String inputType) throws BirtException {

		final DesignConfig config = new DesignConfig();

		final IDesignEngine engine;

		try {
			Platform.startup(config);
			IDesignEngineFactory factory = (IDesignEngineFactory) Platform
					.createFactoryObject(IDesignEngineFactory.EXTENSION_DESIGN_ENGINE_FACTORY);
			engine = factory.createDesignEngine(config);
		} catch (Exception ex) {
			throw ex;
		}

		SessionHandle session = engine.newSessionHandle(ULocale.ENGLISH);
		ReportDesignHandle design = session.createDesign();

		CssStyleSheetHandle css = design.openCssStyleSheet("birt-excel-comparison-report.css");
		design.addCss(css);
//
		//ElementFactory elementFactory = design.getElementFactory();
//
//		design.setTitle("Birt Excel Comparison Report");
//
//		// create report title
//		TextItemHandle title = elementFactory.newTextItem("title");
//		title.setProperty("contentType", "HTML");
//		title.setContent("Birt Excel Comparison Report");
//		design.getBody().add(title);
//
//		// add a line break
//		TextItemHandle lineBreak = elementFactory.newTextItem(null);
//		lineBreak.setProperty("contentType", "HTML");
//		lineBreak.setContent("<br><br>");
//		design.getBody().add(lineBreak);
//
//		// parameter grid
//		GridHandle paramGrid = elementFactory.newGridItem("ParameterGrid", 1, 2);
//		design.getBody().add(paramGrid);
//
//		// add a line break
//		lineBreak = elementFactory.newTextItem(null);
//		lineBreak.setProperty("contentType", "HTML");
//		lineBreak.setContent("<br><br>");
//		design.getBody().add(lineBreak);
//
//		// add run date and time
//		TextItemHandle runDate = elementFactory.newTextItem("runDate");
//		runDate.setProperty("contentType", "HTML");
//		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss");
//		runDate.setContent("Run Date: " + dateFormat.format(new Date()));
//		design.getBody().add(runDate);
//
//		// add a line break
//		lineBreak = elementFactory.newTextItem(null);
//		lineBreak.setProperty("contentType", "HTML");
//		lineBreak.setContent("<br>");
//		design.getBody().add(lineBreak);
//
//		// preparing summary grid
//
//		// add table title
//		TextItemHandle tableTitle = elementFactory.newTextItem("tableTitle");
//		tableTitle.setProperty("contentType", "HTML");
//		tableTitle.setContent("<h3>Summary Grid<h3>");
//		design.getBody().add(tableTitle);
//
//		// add a line break
//		lineBreak = elementFactory.newTextItem(null);
//		lineBreak.setProperty("contentType", "HTML");
//		lineBreak.setContent("<br>");
//		design.getBody().add(lineBreak);
//
//		String[] headers = null;
//
//		if (inputType.equals("xlsx")) {
//			String[] headers = { "S.No.", "Sheet Name", "Mismatch Type", "Mismatch on Column", "Mismatch on Row",
//					"Source 1", "Source 2" };
//			headers = header;
//		} else if (inputType.equals("plain-text")) {
//			String[] header = { "S.No.", "Mismatch Type", "Row Number", "Column Number", "Source 1", "Source 2" };
//			headers = header;
//		}
//
//		TextItemHandle text = elementFactory.newTextItem(null);
//
//		GridHandle summaryGrid = elementFactory.newGridItem("SummaryGrid", headers.length, 1);
//		for (int i = 0; i < headers.length; i++) {
//			CellHandle cell = summaryGrid.getCell(1, i + 1);
//			text = elementFactory.newTextItem(null);
//			text.setProperty("contentType", "HTML");
//			text.setContent(headers[i]);
//			cell.getContent().add(text);
//		}
//
//		// setting width for fixed column
//		if (inputType.equals("xlsx")) {
//			DesignElementHandle col1 = summaryGrid.getColumns().get(0);
//			col1.setProperty("width", ".5");
//			DesignElementHandle col3 = summaryGrid.getColumns().get(2);
//			col3.setProperty("width", "1.7");
//			DesignElementHandle col5 = summaryGrid.getColumns().get(4);
//			col5.setProperty("width", "1.7");
//		}
//
//		design.getBody().add(summaryGrid);
//
//		// add a line break
//		lineBreak = elementFactory.newTextItem(null);
//		lineBreak.setProperty("contentType", "HTML");
//		lineBreak.setContent("<br>");
//		design.getBody().add(lineBreak);
//
//		if (inputType.equals("HTML")) {
//			text = elementFactory.newTextItem(null);
//			text.setProperty("contentType", "HTML");
//			text.setContent("<a href=\"/restart\">Click here to Reset Application and Compare</a>");
//			design.getBody().add(text);
//		}

//		SimpleMasterPageHandle masterPage = elementFactory.newSimpleMasterPage("Master Page");
//		masterPage.setPageType("custom");
//		masterPage.setProperty("width", "100%");
//
//		design.getMasterPages().add(masterPage);

		return design;
	}
}
