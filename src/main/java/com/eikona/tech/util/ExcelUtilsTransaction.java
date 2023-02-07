package com.eikona.tech.util;




import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.eikona.tech.entity.Transaction;


@Component
public class ExcelUtilsTransaction {


// excel file of Timelog
	public static List<Transaction> parseExcelFileTimelog(InputStream is) throws Exception {
		try {
			SimpleDateFormat inputFormat24 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat timeformat=new SimpleDateFormat("HH:mm:ss");
			Workbook workbook = new XSSFWorkbook(is);

			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rows = sheet.iterator();

			List<Transaction> transaction = new ArrayList<Transaction>();

			int rowNumber = 0;
			while (rows.hasNext()) {
				Row currentRow = rows.next();

// skip header
				if (rowNumber == 0) {
					rowNumber++;
					continue;
				}

				Iterator<Cell> cellsInRow = currentRow.iterator();

				Transaction trans = new Transaction();
				Date transactionDate = new Date();
				int cellIndex = 0;
				while (cellsInRow.hasNext()) {
					Cell currentCell = cellsInRow.next();
					cellIndex = currentCell.getColumnIndex();
					if (cellIndex == 0) {
						trans.setLogId((int) currentCell.getNumericCellValue());
					} else if (cellIndex == 1) {
						currentCell.setCellType(CellType.STRING);
						if (currentCell.getCellType() == CellType.NUMERIC) {
							trans.setEmployeeCode(String.valueOf(currentCell.getNumericCellValue()));
						} else if (currentCell.getCellType() == CellType.STRING) {
							trans.setEmployeeCode(currentCell.getStringCellValue());
						}
					} else if (cellIndex == 2) {
						currentCell.setCellType(CellType.STRING);
						if (currentCell.getCellType() == CellType.NUMERIC) {
							trans.setEmpId(String.valueOf(currentCell.getNumericCellValue()));
						} else if (currentCell.getCellType() == CellType.STRING) {
							trans.setEmpId(currentCell.getStringCellValue());
						}
					} else if (cellIndex == 3) {
						//trans.setName(currentCell.getStringCellValue());
						currentCell.setCellType(CellType.STRING);
						if (currentCell.getCellType() == CellType.NUMERIC) {
							trans.setName(String.valueOf(currentCell.getNumericCellValue()));
						} else if (currentCell.getCellType() == CellType.STRING) {
							trans.setName(currentCell.getStringCellValue());
						}
						
						/*
						 * if(null == trans.getName()|| trans.getName().isEmpty()) { break; }
						 */
					} else if (cellIndex == 4) {
						//trans.setDeviceId( currentCell.getNumericCellValue());
						if (currentCell.getCellType() == CellType.NUMERIC) {
							trans.setDeviceId(currentCell.getNumericCellValue());
						} else if (currentCell.getCellType() == CellType.STRING) {
							trans.setDeviceId(Double.parseDouble(currentCell.getStringCellValue()));
						}
					} else if (cellIndex == 5) {
						
						if (currentCell.getCellType() == CellType.STRING) {
							String transactionDateStr = currentCell.getStringCellValue().trim();
							System.out.println(transactionDateStr);
							if(transactionDateStr.contains("AM") || transactionDateStr.contains("PM")) {
								transactionDate = inputFormat.parse(transactionDateStr);
							}else {
								transactionDate = inputFormat24.parse(transactionDateStr);
							}
						} else {
							transactionDate=(Date) currentCell.getDateCellValue();
						}
						
						
						trans.setPunchTimeStr(timeformat.format(transactionDate));
						trans.setPunchTime(timeformat.parse(trans.getPunchTimeStr()));
						trans.setPunchDate(transactionDate);
						trans.setPunchDateStr(dateFormat.format(transactionDate));
					}
					else if (cellIndex == 6) {
						trans.setDeviceName(currentCell.getStringCellValue());
						
						if(trans.getDeviceName().contains("HYD") || trans.getDeviceName().contains("MUM") ||  trans.getDeviceName().contains("NOIDA")) {
							trans.setOrganization("Tata Projects Limited");
						}else if(trans.getDeviceName().contains("NMDC")) {
							trans.setOrganization("NMDC Nagarnar");
						}else{
							trans.setOrganization("ESL Bokaro");
						}
					}
					trans.setAccessType("Face");
				}

				transaction.add(trans);
			}

// Close WorkBook
			workbook.close();

			return transaction;
		} catch (IOException e) {
			throw new RuntimeException("FAIL! -> message = " + e.getMessage());
		}
	}

}
