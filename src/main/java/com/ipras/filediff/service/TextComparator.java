package com.ipras.filediff.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.model.api.CellHandle;
import org.eclipse.birt.report.model.api.ElementFactory;
import org.eclipse.birt.report.model.api.GridHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.RowOperationParameters;
import org.eclipse.birt.report.model.api.TextItemHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.ipras.filediff.design.ReportDesigner;

public class TextComparator {

	@Autowired
	private ReportDesigner reportDesigner;

	@Value("${input.file1.path}")
	private String sourcePath1;
	@Value("${input.file2.path}")
	private String sourcePath2;

	@Value("${input.file.delimiter}")
	private String delimiter;

	@Value("${output.file.path}")
	private String tempPath;

	@Value("${header.row.count}")
	private String headerCount;

	@Value("${footer.row.count}")
	private String footerCount;
	
	@Value("${file.copy.encoding}")
	private String fileEncoding;
	

	private ReportDesignHandle design;
	private ElementFactory factory;
	int detailGridRowCount = 0;
	int summaryGridRowCount = 0;

	List<Integer> columnBreakCounts;

	public ReportDesignHandle compareText()
			throws BirtException, EncryptedDocumentException, InvalidFormatException, IOException, URISyntaxException {

		design = reportDesigner.buildReport("plain-text");
		factory = design.getElementFactory();

		// add input parameters to grid
		// addInputParameters();

		int[] fileNotFound = { 0, 0 };

		// begin excel comparison

		// load source file
		FileInputStream file1 = null;
		try {
			file1 = new FileInputStream(new File(sourcePath1));
		} catch (FileNotFoundException e) {
			fileNotFound[0] = 1;
		}
		FileInputStream file2 = null;
		try {
			file2 = new FileInputStream(new File(sourcePath2));
		} catch (FileNotFoundException e) {
			fileNotFound[1] = 1;
		}

		if (fileNotFound[0] == 1 || fileNotFound[1] == 1) {
			fileNotFoundMismatch(fileNotFound);
			return design;
		}

		prepareFileForCompare(sourcePath1, tempPath + "/source1Copy.csv", Integer.parseInt(headerCount),
				Integer.parseInt(footerCount));

		prepareFileForCompare(sourcePath2, tempPath + "/source2Copy.csv", Integer.parseInt(headerCount),
				Integer.parseInt(footerCount));

		if (Integer.parseInt(headerCount) == 1) {
			addFileHeaders(sourcePath1);
		}

		// compare();
		LineIterator leftFile = FileUtils.lineIterator(new File(tempPath + "/source1Copy.csv"), fileEncoding);
		LineIterator rightFile = FileUtils.lineIterator(new File(tempPath + "/source2Copy.csv"), fileEncoding);

		int rowNum = 0;
		boolean rowCountMismatch = false;

		while (leftFile.hasNext() || rightFile.hasNext()) {

			rowNum++;

			if (leftFile.hasNext() != rightFile.hasNext()) {
				System.out.println("Warning: Row Number does not match between the files!!!");
				rowCountMismatch = true;
				break;
			}

			String left = leftFile.nextLine();
			String right = rightFile.nextLine();

			compare(rowNum, Arrays.asList(left.split(Pattern.quote(delimiter))), Arrays.asList(right.split(Pattern.quote(delimiter))));
		}

		if (!rowCountMismatch) {
			createSummaryGrid();
		}

		return design;
	}

	private void createSummaryGrid() throws SemanticException {

		GridHandle grid = (GridHandle) design.findElement("SummaryGrid");
		RowOperationParameters rowParam = new RowOperationParameters(1, 0, 1);
		grid.insertRow(rowParam);

		for (int i = 0; i < columnBreakCounts.size(); i++) {
			CellHandle cell = grid.getCell(2, i + 1);
			TextItemHandle textElement = factory.newTextItem(null);
			textElement.setContent(Integer.toString(columnBreakCounts.get(i)));
			cell.getContent().add(textElement);
			cell.setProperty("style", "cell");
		}

	}

	private void compare(int rowNum, List<String> left, List<String> right) throws IOException, SemanticException {

		//System.out.println(left);
		//System.out.println(right);
		
		
		if (left.size() != right.size()) {
			// addColumnMismatch(rowNum, left.size(), right.size());
			System.out.println("Warning: Column Count does not match between the files for Row Number " + rowNum);
			return;
		}

		List<String> leftMismatch = IntStream.range(0, left.size()).filter(i -> !left.get(i).equals(right.get(i)))
				.mapToObj(i -> left.get(i)).collect(Collectors.toList());

		List<String> rightMismatch = IntStream.range(0, right.size()).filter(i -> !right.get(i).equals(left.get(i)))
				.mapToObj(i -> right.get(i)).collect(Collectors.toList());

		List<Integer> mismatchColumnIndex = IntStream.range(0, left.size())
				.mapToObj(i -> left.get(i) + "::" + (i + 1) + "::" + right.get(i))
				.filter(e -> !e.split("::")[0].equals(e.split("::")[2]))
				.mapToInt(e -> Integer.valueOf(e.split("::")[1])).mapToObj(e -> e).collect(Collectors.toList());

		if (mismatchColumnIndex.size() == 0) {
			return;
		}

		 //System.out.println(leftMismatch);
		 //System.out.println(rightMismatch);
		 //System.out.println(mismatchColumnIndex);

		addLineMismatch(rowNum, leftMismatch, rightMismatch, mismatchColumnIndex);

	}

	private void addLineMismatch(int rowNum, List<String> leftMismatch, List<String> rightMismatch,
			List<Integer> mismatchColumnIndex) throws SemanticException {

		detailGridRowCount++;

		GridHandle grid = (GridHandle) design.findElement("DetailGrid");
		RowOperationParameters rowParam = new RowOperationParameters(1, 0, detailGridRowCount - 1);
		grid.insertRow(rowParam);

		CellHandle cell = grid.getCell(detailGridRowCount, 1);
		TextItemHandle textElement = factory.newTextItem(null);
		textElement.setContent(Integer.toString(rowNum));
		cell.getContent().add(textElement);
		cell.setProperty("style", "cell");

		for (int i = 0; i < mismatchColumnIndex.size(); i++) {
			cell = grid.getCell(detailGridRowCount, mismatchColumnIndex.get(i)+1);
			textElement = factory.newTextItem(null);
			textElement.setContent(leftMismatch.get(i));
			cell.getContent().add(textElement);
			cell.setProperty("style", "cell");
		}

		detailGridRowCount++;

		RowOperationParameters rowParam2 = new RowOperationParameters(1, 0, detailGridRowCount - 1);
		grid.insertRow(rowParam2);

		cell = grid.getCell(detailGridRowCount, 1);
		textElement = factory.newTextItem(null);
		textElement.setContent(Integer.toString(rowNum));
		cell.getContent().add(textElement);
		cell.setProperty("style", "cell");

		for (int i = 0; i < mismatchColumnIndex.size(); i++) {
			cell = grid.getCell(detailGridRowCount, mismatchColumnIndex.get(i)+1);
			textElement = factory.newTextItem(null);
			textElement.setContent(rightMismatch.get(i));
			cell.getContent().add(textElement);
			cell.setProperty("style", "cell");
		}

		for (int i = 0; i < mismatchColumnIndex.size(); i++) {
			columnBreakCounts.set(mismatchColumnIndex.get(i) - 1,
					columnBreakCounts.get(mismatchColumnIndex.get(i) - 1) + 1);
		}

	}

	private void addFileHeaders(String sourcePath) throws IOException, SemanticException {

		detailGridRowCount++;

		Path path = Paths.get(sourcePath);
		

		Charset cs = Charset.forName(fileEncoding);  

		Stream<String> lines = Files.lines(path, cs);
		

		String[] headers = lines.limit(1).collect(Collectors.joining()).split(Pattern.quote(delimiter));

		for(String e : headers) {
			//System.out.println(e);
		}
		
		TextItemHandle text = factory.newTextItem(null);

		GridHandle detailGrid = factory.newGridItem("DetailGrid", headers.length + 1, 1);

		CellHandle cell = detailGrid.getCell(1, 1);
		text = factory.newTextItem(null);
		text.setContent("Row Number");
		cell.getContent().add(text);
		cell.setProperty("style", "header-cell");

		for (int i = 0; i < headers.length; i++) {
			cell = detailGrid.getCell(1, i + 2);
			text = factory.newTextItem(null);
			text.setContent(headers[i]);
			cell.getContent().add(text);
			cell.setProperty("style", "header-cell");
		}

		design.getBody().add(detailGrid);

		GridHandle summaryGrid = factory.newGridItem("SummaryGrid", headers.length, 1);

		for (int i = 0; i < headers.length; i++) {
			cell = summaryGrid.getCell(1, i + 1);
			text = factory.newTextItem(null);
			text.setContent(headers[i]);
			cell.getContent().add(text);
			cell.setProperty("style", "header-cell");
		}

		design.getBody().add(summaryGrid);

		// initializing counter array to get per column breaks
		columnBreakCounts = new ArrayList<>(Arrays.asList(new Integer[headers.length]));
		Collections.fill(columnBreakCounts, 0);

		lines.close();
	}

	private void prepareFileForCompare(String filePath, String fileCopyPath, int headerCount, int footerCount)
			throws URISyntaxException, IOException {

		Path path = Paths.get(filePath);

		Path copyPath = Paths.get(fileCopyPath);
		
		Charset cs = Charset.forName(fileEncoding);  

		Stream<String> lines = Files.lines(path, cs);

		int rowCount = (int) Files.lines(path, cs).count();

		List<String> sortedLines = lines.limit(rowCount - footerCount).skip(headerCount).sorted()
				.collect(Collectors.toList());

		Files.write(copyPath, sortedLines);

		// Files.delete(copyPath);

		lines.close();

		// System.out.println(sortedLines);

	}

	private void fileNotFoundMismatch(int[] fileNotFound) throws SemanticException {

		GridHandle paramGrid = (GridHandle) design.findElement("DetailGrid");
		paramGrid.drop();
		TextItemHandle text = factory.newTextItem(null);
		text.setProperty("contentType", "HTML");

		if (fileNotFound[0] == 1 && fileNotFound[1] == 1) {
			text.setContent("Source File 1 and 2 was not found on the path specified.");
		} else if (fileNotFound[0] == 1) {
			text.setContent("Source File 1 was not found on the path specified.");
		} else if (fileNotFound[1] == 1) {
			text.setContent("Source File 2 was not found on the path specified.");
		}

		text.setProperty("style", "open-cell");

		design.getBody().add(text);

	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public String getSourcePath1() {
		return sourcePath1;
	}

	public void setSourcePath1(String sourcePath1) {
		this.sourcePath1 = sourcePath1;
	}

	public String getSourcePath2() {
		return sourcePath2;
	}

	public void setSourcePath2(String sourcePath2) {
		this.sourcePath2 = sourcePath2;
	}

}
