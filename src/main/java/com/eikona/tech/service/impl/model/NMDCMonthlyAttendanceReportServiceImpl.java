package com.eikona.tech.service.impl.model;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.AreaConstants;
import com.eikona.tech.constants.DailyAttendanceConstants;
import com.eikona.tech.constants.EmployeeConstants;
import com.eikona.tech.constants.HeaderConstants;
import com.eikona.tech.constants.NMDCConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.dto.NMDCMonthlyAttendanceDto;
import com.eikona.tech.dto.NMDCReportDto;
import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.DailyReport;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.repository.DailyAttendanceRepository;
import com.eikona.tech.repository.EmployeeRepository;
import com.eikona.tech.util.CalendarUtil;
import com.eikona.tech.util.GeneralSpecificationUtil;


@Service
public class NMDCMonthlyAttendanceReportServiceImpl{
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private DailyAttendanceRepository dailyAttendanceRepository;
	
	@Autowired
	private CalendarUtil calendarUtil;
	
	@Autowired
	private GeneralSpecificationUtil<Employee> generalSpecificationEmployee;
	
	public NMDCReportDto<NMDCMonthlyAttendanceDto> calculateMonthlyReport(String startDateStr, String employeeId, String employeeName, String department, String designation, String company, String orgname) {
		
		NMDCReportDto<NMDCMonthlyAttendanceDto> monthlyDetailsReport = new NMDCReportDto<>();
		
		if(startDateStr.isEmpty()) {
			startDateStr = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US).format(new Date());
		}
		else {
			startDateStr=startDateStr+"-01";
		}
		try {

			Date date = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US).parse(startDateStr);
			Calendar dateCalendar = Calendar.getInstance(); 
			dateCalendar.setTime(date);
			String month = dateCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH );
			
			int first = dateCalendar.getActualMinimum(Calendar.DATE), day = dateCalendar.getActualMinimum(Calendar.DATE);
			int last = dateCalendar.getActualMaximum(Calendar.DATE);
			List<String> headList = getHeadList(month, day, last);
			
			Date startCalendar = calendarUtil.getConvertedDate(date, first, NumberConstants.ZERO, NumberConstants.ZERO, NumberConstants.ZERO);
			
			Date endCalendar = calendarUtil.getConvertedDate(date,last, NumberConstants.TWENTY_THREE, NumberConstants.FIFTY_NINE, NumberConstants.FIFTY_NINE);
			
			Specification<Employee> isDeletedFalse = generalSpecificationEmployee.isDeletedSpecification();
			Specification<Employee> nameSpc = generalSpecificationEmployee.stringSpecification(employeeName, ApplicationConstants.NAME);
			Specification<Employee> empIdSpc = generalSpecificationEmployee.stringSpecification(employeeId, EmployeeConstants.EMPID);
			Specification<Employee>companySpc = generalSpecificationEmployee.stringSpecification(company, DailyAttendanceConstants.COMPANY);
			Specification<Employee> deptSpec = generalSpecificationEmployee.foreignKeyStringSpecification(department, EmployeeConstants.DEPARTMENT,ApplicationConstants.NAME);
			Specification<Employee> designationSpc = generalSpecificationEmployee.foreignKeyStringSpecification(designation, EmployeeConstants.DESIGNATION,ApplicationConstants.NAME);
			Specification<Employee> orgSpc = generalSpecificationEmployee.foreignKeyStringSpecification(orgname, AreaConstants.ORGANIZATION, ApplicationConstants.NAME);
			List<Employee> workerList = employeeRepository.findAll(isDeletedFalse.and(designationSpc).and(companySpc).and(orgSpc).and(empIdSpc).and(nameSpc).and(deptSpec));
			
			List<NMDCMonthlyAttendanceDto> monthlyReportList = new ArrayList<>();
			for(Employee employee: workerList) {
				NMDCMonthlyAttendanceDto monthlyDetailDto = calculateDaywiseMonthlyReport(startCalendar, endCalendar, employee);
				monthlyReportList.add(monthlyDetailDto);
				
			}
			
			monthlyDetailsReport.setHeadList(headList);
			monthlyDetailsReport.setDataList(monthlyReportList);
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return monthlyDetailsReport;
	}


	private List<String> getHeadList(String month, int day, int last) {
		List<String> headList = new ArrayList<String>();
		headList.add(NMDCConstants.EMPLOYEE_ID);
		headList.add(NMDCConstants.NAME);
		headList.add(HeaderConstants.COMPANY);
		headList.add(HeaderConstants.DEPARTMENT);
		headList.add(HeaderConstants.DESIGNATION);
		headList.add(HeaderConstants.GRADE);
		headList.add(HeaderConstants.MOBILE_NO);
		while(day <= last) {
			headList.add(day+ApplicationConstants.DELIMITER_SPACE+month.substring(NumberConstants.ZERO, NumberConstants.THREE));
			day++;
		}
		headList.add(ApplicationConstants.TOTAL_PRESENT);
		headList.add(NMDCConstants.TOTAL_OVERTIME);
		return headList;
	}
	
	 
	public NMDCMonthlyAttendanceDto calculateDaywiseMonthlyReport(Date startDate, Date endDate, Employee employee){
		 
		Calendar calender = Calendar.getInstance();
		calender.setTime(endDate);
		int lastDayOfMonth = calender.get(Calendar.DATE);
		
		List<DailyReport> dailyReportList = dailyAttendanceRepository.findDetailsByDateCustom(employee.getEmpId(), startDate, endDate);
		
		NMDCMonthlyAttendanceDto monthlyDailyReportDto = new NMDCMonthlyAttendanceDto();
		
		//set employee details in monthly report
		setEmployeeDetailsInNMDCMonthlyReport(employee, monthlyDailyReportDto);
			
		Iterator<DailyReport> dailyReportListItr = dailyReportList.iterator();
		DailyReport dailyAttendance = null;
		if (dailyReportListItr.hasNext()) {
			dailyAttendance = dailyReportListItr.next();
		}
		
		Calendar startDayCalendar = Calendar.getInstance(); 
		startDayCalendar.setTime(startDate);
		int first = startDayCalendar.getActualMinimum(Calendar.DATE);
		
		Calendar endDayCalendar = Calendar.getInstance(); 
		endDayCalendar.setTime(endDate);
		int last = endDayCalendar.getActualMaximum(Calendar.DATE);
		
		//set monthly report data
		setNMDCNMDCReportDto(lastDayOfMonth, monthlyDailyReportDto, dailyReportListItr, dailyAttendance, first,
				last);
		
		return monthlyDailyReportDto;
		 
	 }


	private void setNMDCNMDCReportDto(int lastDayOfMonth, NMDCMonthlyAttendanceDto monthlyDailyReportDto,
			Iterator<DailyReport> dailyReportListItr, DailyReport dailyAttendance, int first, int last) {
		Integer totalPresentCount = NumberConstants.ZERO;
		
		Integer totalAbsentCount = NumberConstants.ZERO;
		
		long totalOverTime = NumberConstants.LONG_ZERO;
		
		List<String> dataList = new ArrayList<String>();
		while(first <= last) {
			if(null != dailyAttendance) {
				String[] dateArray=dailyAttendance.getDateStr().split("-");
				String date=dateArray[2];
				if(Integer.valueOf(date)==first) {
					if(NMDCConstants.PRESENT.equalsIgnoreCase(dailyAttendance.getAttendanceStatus())) 
						totalPresentCount += NumberConstants.ONE;
					else if(NMDCConstants.ABSENT.equalsIgnoreCase(dailyAttendance.getAttendanceStatus()))
						totalAbsentCount += NumberConstants.ONE;
					
					if(null!=dailyAttendance.getOverTime())
						totalOverTime+=dailyAttendance.getOverTime();

					if(NMDCConstants.ABSENT.equalsIgnoreCase(dailyAttendance.getAttendanceStatus()))
						dataList.add(NMDCConstants.ABSENT);
					else if(NMDCConstants.PRESENT.equalsIgnoreCase(dailyAttendance.getAttendanceStatus()))
						dataList.add("P");
					else if(null == dailyAttendance.getAttendanceStatus() )
							dataList.add(ApplicationConstants.DELIMITER_HYPHEN);
					
					if (dailyReportListItr.hasNext()) {
						dailyAttendance = dailyReportListItr.next();
					}
				}
					else 
						dataList.add(ApplicationConstants.DELIMITER_HYPHEN);
				}
			 else{
				dataList.add(ApplicationConstants.DELIMITER_HYPHEN);
			}
			
			first++;
		}
		
		monthlyDailyReportDto.setTotalPresentCount(String.valueOf(totalPresentCount));
		monthlyDailyReportDto.setTotalAbsentCount(String.valueOf(lastDayOfMonth-totalPresentCount));
		
		monthlyDailyReportDto.setTotalOverTime(String.valueOf(totalOverTime/60) + ":" + String.valueOf(totalOverTime % 60));
		
		monthlyDailyReportDto.setTotalDays(String.valueOf(lastDayOfMonth));
		
		monthlyDailyReportDto.setDateList(dataList);
	}


	private void setEmployeeDetailsInNMDCMonthlyReport(Employee employee,
			NMDCMonthlyAttendanceDto monthlyDailyReportDto) {
		if(null != employee) {
			
			if(null != employee.getEmpId())
				monthlyDailyReportDto.setEmpId(employee.getEmpId());
			else
				monthlyDailyReportDto.setEmpId(ApplicationConstants.DELIMITER_EMPTY);
			monthlyDailyReportDto.setEmpName(employee.getName());
			if(null != employee.getDepartment())
				monthlyDailyReportDto.setDepartment(employee.getDepartment().getName());
			else
				monthlyDailyReportDto.setDepartment(ApplicationConstants.DELIMITER_EMPTY);
			if(null != employee.getDesignation())
				monthlyDailyReportDto.setDesignation(employee.getDesignation().getName());
			else
				monthlyDailyReportDto.setDesignation(ApplicationConstants.DELIMITER_EMPTY);
		   monthlyDailyReportDto.setGrade(employee.getGrade());
		   monthlyDailyReportDto.setMobile(employee.getMobile());
		   monthlyDailyReportDto.setCompany(employee.getCompany());
			
		}else {
			monthlyDailyReportDto.setEmpId(ApplicationConstants.DELIMITER_EMPTY);
			monthlyDailyReportDto.setEmpName(ApplicationConstants.DELIMITER_EMPTY);
			monthlyDailyReportDto.setGrade(ApplicationConstants.DELIMITER_EMPTY);
			monthlyDailyReportDto.setMobile(ApplicationConstants.DELIMITER_EMPTY);
			monthlyDailyReportDto.setCompany(ApplicationConstants.DELIMITER_EMPTY);
			monthlyDailyReportDto.setDepartment(ApplicationConstants.DELIMITER_EMPTY);
			monthlyDailyReportDto.setDesignation(ApplicationConstants.DELIMITER_EMPTY);
		}
	}
	 
	 public void excelGenerator(HttpServletResponse response, NMDCReportDto<NMDCMonthlyAttendanceDto> monthlyDetailsList)
				throws ParseException, IOException {

		DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_INDIA_SPLIT_BY_UNDERSCORE);
		String currentDateTime = dateFormat.format(new Date());
		String filename = NMDCConstants.MONTHLY_ATTENDANCE + currentDateTime + ApplicationConstants.EXTENSION_EXCEL;
		Workbook workBook = new XSSFWorkbook();
		Sheet sheet = workBook.createSheet();

		int rowCount = NumberConstants.ZERO;
		Row row = sheet.createRow(rowCount++);

		Font font = workBook.createFont();
		font.setBold(true);

		//set border style for header data
		CellStyle cellStyle = setExcelBorderStyle(workBook, BorderStyle.THICK, font);

		int index=NumberConstants.ZERO;
		Cell cell = row.createCell(NumberConstants.ZERO);
		List<String> headList = monthlyDetailsList.getHeadList();
		for(String head : headList) {
			cell = row.createCell(index++);
			cell.setCellValue(head);
			cell.setCellStyle(cellStyle);
		}
		
		
		font = workBook.createFont();
		font.setBold(false);

		//set border style for body data
		cellStyle = setExcelBorderStyle(workBook, BorderStyle.THIN, font);

		List<NMDCMonthlyAttendanceDto> incapMonthlyDetailDtoList = monthlyDetailsList.getDataList();
		//set excel data for incap monthly report
		setExcelDataForNMDCMonthlyReport(sheet, rowCount, cellStyle, incapMonthlyDetailDtoList);

		FileOutputStream fileOut = new FileOutputStream(filename);
		workBook.write(fileOut);
		ServletOutputStream outputStream = response.getOutputStream();
		workBook.write(outputStream);
		fileOut.close();
		workBook.close();

	}


	private void setExcelDataForNMDCMonthlyReport(Sheet sheet, int rowCount, CellStyle cellStyle,
			List<NMDCMonthlyAttendanceDto> incapMonthlyDetailDtoList) {
		
		for (NMDCMonthlyAttendanceDto monthlyDetail : incapMonthlyDetailDtoList) {
			if(rowCount==90000)
				break;
			Row row = sheet.createRow(rowCount++);

			int columnCount = NumberConstants.ZERO;

			Cell cell = row.createCell(columnCount++);
			cell.setCellValue(monthlyDetail.getEmpId());
			cell.setCellStyle(cellStyle);

			cell = row.createCell(columnCount++);
			cell.setCellValue(monthlyDetail.getEmpName());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(monthlyDetail.getCompany());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(monthlyDetail.getDepartment());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(monthlyDetail.getDesignation());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(monthlyDetail.getGrade());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(monthlyDetail.getMobile());
			cell.setCellStyle(cellStyle);
			
			//month
			for(String data: monthlyDetail.getDateList()) {
				cell = row.createCell(columnCount++);
				cell.setCellValue(data);
				cell.setCellStyle(cellStyle);
			}
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(monthlyDetail.getTotalPresentCount());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(monthlyDetail.getTotalOverTime());
			cell.setCellStyle(cellStyle);
			
		
		}
	}


	private CellStyle setExcelBorderStyle(Workbook workBook, BorderStyle borderStyle, Font font) {
		CellStyle cellStyle = workBook.createCellStyle();
		cellStyle.setBorderTop(borderStyle);
		cellStyle.setBorderBottom(borderStyle);
		cellStyle.setBorderLeft(borderStyle);
		cellStyle.setBorderRight(borderStyle);
		cellStyle.setFont(font);
		return cellStyle;
	}

	public PaginationDto<NMDCReportDto<NMDCMonthlyAttendanceDto>> search(String dateStr, String employeeId, String employeeName, String department, String designation, String company, int pageno, String sortField,
			String sortDir, String orgname) {

		PaginationDto<NMDCReportDto<NMDCMonthlyAttendanceDto>> dtoList = new PaginationDto<>();
		NMDCReportDto<NMDCMonthlyAttendanceDto> monthlyDetailsReport = new NMDCReportDto<>();
		Date date = null;
		SimpleDateFormat format = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US);
		if(dateStr.isEmpty()) {
			dateStr = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US).format(new Date());
		}
		else {
			dateStr=dateStr+"-01";
		}
			try {

				date = format.parse(dateStr);
				Calendar dateCalendar = Calendar.getInstance(); 
				dateCalendar.setTime(date);
				String month = dateCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH );
				
				int first = dateCalendar.getActualMinimum(Calendar.DATE), day = dateCalendar.getActualMinimum(Calendar.DATE);
				int last = dateCalendar.getActualMaximum(Calendar.DATE);
				
				List<String> headList = getHeadList(month, day, last);

				Date startCalendar = calendarUtil.getConvertedDate(date, first, NumberConstants.ZERO, NumberConstants.ZERO, NumberConstants.ZERO);
				
				Date endCalendar = calendarUtil.getConvertedDate(date,last, NumberConstants.TWENTY_THREE, NumberConstants.FIFTY_NINE, NumberConstants.FIFTY_NINE);
				
				if (null == sortDir || sortDir.isEmpty()) {
					sortDir = ApplicationConstants.ASC;
				}
				if (null == sortField || sortField.isEmpty()) {
					sortField = ApplicationConstants.ID;
				}
				Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
						: Sort.by(sortField).descending();

				Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
				
		    	
		    	Specification<Employee> isDeletedFalse = generalSpecificationEmployee.isDeletedSpecification();
				Specification<Employee> nameSpc = generalSpecificationEmployee.stringSpecification(employeeName, ApplicationConstants.NAME);
				Specification<Employee> empIdSpc = generalSpecificationEmployee.stringSpecification(employeeId, EmployeeConstants.EMPID);
				Specification<Employee>companySpc = generalSpecificationEmployee.stringSpecification(company, DailyAttendanceConstants.COMPANY);
				Specification<Employee> deptSpec = generalSpecificationEmployee.foreignKeyStringSpecification(department, EmployeeConstants.DEPARTMENT,ApplicationConstants.NAME);
				Specification<Employee> designationSpc = generalSpecificationEmployee.foreignKeyStringSpecification(designation, EmployeeConstants.DESIGNATION,ApplicationConstants.NAME);
				Specification<Employee> orgSpc = generalSpecificationEmployee.foreignKeyStringSpecification(orgname, AreaConstants.ORGANIZATION, ApplicationConstants.NAME);
				Page<Employee> page = employeeRepository.findAll(isDeletedFalse.and(designationSpc).and(orgSpc).and(companySpc).and(empIdSpc).and(nameSpc).and(deptSpec),pageable);
				setMonthlyAttendanceDtoList(monthlyDetailsReport, headList, startCalendar, endCalendar, page);

				sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir)) ? ApplicationConstants.DESC : ApplicationConstants.ASC;
				List<NMDCReportDto<NMDCMonthlyAttendanceDto>> monthlyDetailsReportList = new ArrayList<NMDCReportDto<NMDCMonthlyAttendanceDto>>();
				monthlyDetailsReportList.add(monthlyDetailsReport);
				dtoList = new PaginationDto<>(monthlyDetailsReportList, page.getTotalPages(), page.getNumber() + NumberConstants.ONE,
						page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);

			} catch (ParseException e) {
				e.printStackTrace();
			}
		return dtoList;
	}


	private void setMonthlyAttendanceDtoList(NMDCReportDto<NMDCMonthlyAttendanceDto> monthlyDetailsReport,
			List<String> headList, Date startCalendar, Date endCalendar, Page<Employee> page) {
		List<Employee> workerList = page.getContent();
		List<NMDCMonthlyAttendanceDto> monthlyReportList = new ArrayList<>();
		for (Employee employee : workerList) {
			NMDCMonthlyAttendanceDto monthlyDetailDto = calculateDaywiseMonthlyReport(startCalendar, endCalendar, employee);
			monthlyReportList.add(monthlyDetailDto);
		}

		monthlyDetailsReport.setHeadList(headList);
		monthlyDetailsReport.setDataList(monthlyReportList);
	}
	
	//---------------------------------------------------------------In & out monthly report-----------------------------------------------------------------//
	
		public NMDCReportDto<NMDCMonthlyAttendanceDto> calculateMonthlyReportWithInOutTime(String startDateStr, String employeeId, String employeeName, String department, String designation,String company, String organization) {
				
				NMDCReportDto<NMDCMonthlyAttendanceDto> monthlyDetailsReport = new NMDCReportDto<>();
				
				if(startDateStr.isEmpty()) {
					startDateStr = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US).format(new Date());
				}
				else {
					startDateStr=startDateStr+"-01";
				}
				try {

					Date date = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US).parse(startDateStr);
					Calendar dateCalendar = Calendar.getInstance(); 
					dateCalendar.setTime(date);
					String month = dateCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH );
					
					int first = dateCalendar.getActualMinimum(Calendar.DATE), day = dateCalendar.getActualMinimum(Calendar.DATE);
					int last = dateCalendar.getActualMaximum(Calendar.DATE);
					List<String> headList = getHeadListWithInOutTime(month, day, last);
					
					Date startCalendar = calendarUtil.getConvertedDate(date, first, NumberConstants.ZERO, NumberConstants.ZERO, NumberConstants.ZERO);
					
					Date endCalendar = calendarUtil.getConvertedDate(date,last, NumberConstants.TWENTY_THREE, NumberConstants.FIFTY_NINE, NumberConstants.FIFTY_NINE);
					
					Specification<Employee> isDeletedFalse = generalSpecificationEmployee.isDeletedSpecification();
					Specification<Employee> nameSpc = generalSpecificationEmployee.stringSpecification(employeeName, ApplicationConstants.NAME);
					Specification<Employee> empIdSpc = generalSpecificationEmployee.stringSpecification(employeeId, EmployeeConstants.EMPID);
					Specification<Employee> deptSpec = generalSpecificationEmployee.foreignKeyStringSpecification(department, EmployeeConstants.DEPARTMENT,ApplicationConstants.NAME);
					Specification<Employee> designationSpc = generalSpecificationEmployee.foreignKeyStringSpecification(designation, EmployeeConstants.DESIGNATION,ApplicationConstants.NAME);
					Specification<Employee> companySpc = generalSpecificationEmployee.stringSpecification(company, DailyAttendanceConstants.COMPANY);
					Specification<Employee> orgSpc = generalSpecificationEmployee.foreignKeyStringSpecification(organization, AreaConstants.ORGANIZATION, ApplicationConstants.NAME);
					List<Employee> workerList = employeeRepository.findAll(isDeletedFalse.and(designationSpc).and(companySpc).and(empIdSpc).and(nameSpc).and(deptSpec).and(orgSpc));
					
					List<NMDCMonthlyAttendanceDto> monthlyReportList = new ArrayList<>();
					for(Employee employee: workerList) {
						NMDCMonthlyAttendanceDto monthlyDetailDto = calculateDaywiseMonthlyReportWithInOutTime(startCalendar, endCalendar, employee);
						monthlyReportList.add(monthlyDetailDto);
						
					}
					
					monthlyDetailsReport.setHeadList(headList);
					monthlyDetailsReport.setDataList(monthlyReportList);
					
				} catch (ParseException e) {
					e.printStackTrace();
				}
				return monthlyDetailsReport;
			}
			
			public NMDCMonthlyAttendanceDto calculateDaywiseMonthlyReportWithInOutTime(Date startDate, Date endDate, Employee employee){
				 
				Calendar calender = Calendar.getInstance();
				calender.setTime(endDate);
				int lastDayOfMonth = calender.get(Calendar.DATE);
				
				List<DailyReport> dailyReportList = dailyAttendanceRepository.findDetailsByDateCustom(employee.getEmpId(), startDate, endDate);
				
				NMDCMonthlyAttendanceDto monthlyDailyReportDto = new NMDCMonthlyAttendanceDto();
				
				//set employee details in monthly report
				setEmployeeDetailsInNMDCMonthlyReport(employee, monthlyDailyReportDto);
					
				Iterator<DailyReport> dailyReportListItr = dailyReportList.iterator();
				DailyReport dailyAttendance = null;
				if (dailyReportListItr.hasNext()) {
					dailyAttendance = dailyReportListItr.next();
				}
				
				Calendar startDayCalendar = Calendar.getInstance(); 
				startDayCalendar.setTime(startDate);
				int first = startDayCalendar.getActualMinimum(Calendar.DATE);
				
				Calendar endDayCalendar = Calendar.getInstance(); 
				endDayCalendar.setTime(endDate);
				int last = endDayCalendar.getActualMaximum(Calendar.DATE);
				
				//set monthly report data
				setNMDCReportDtoWithInOutTime(lastDayOfMonth, monthlyDailyReportDto, dailyReportListItr, dailyAttendance, first,
						last);
				
				return monthlyDailyReportDto;
				 
			 }
			
			private void setNMDCReportDtoWithInOutTime(int lastDayOfMonth, NMDCMonthlyAttendanceDto monthlyDailyReportDto,
					Iterator<DailyReport> dailyReportListItr, DailyReport dailyAttendance, int first, int last) {
				Integer totalPresentCount = NumberConstants.ZERO;
				
				Integer totalAbsentCount = NumberConstants.ZERO;
				
				long totalOverTime = NumberConstants.LONG_ZERO;
				
				List<String> dataList = new ArrayList<String>();
				List<String> inTimeList = new ArrayList<String>();
				List<String> outTimeList = new ArrayList<String>();
				while(first <= last) {
					if(null != dailyAttendance) {
						String[] dateArray=dailyAttendance.getDateStr().split("-");
						String date=dateArray[2];
						if(Integer.valueOf(date)==first) {
							if(NMDCConstants.PRESENT.equalsIgnoreCase(dailyAttendance.getAttendanceStatus()))
								totalPresentCount += NumberConstants.ONE;
							else if(NMDCConstants.ABSENT.equalsIgnoreCase(dailyAttendance.getAttendanceStatus()))
								totalAbsentCount += NumberConstants.ONE;
							
							if(null!=dailyAttendance.getOverTime())
								totalOverTime+=dailyAttendance.getOverTime();

							if(NMDCConstants.ABSENT.equalsIgnoreCase(dailyAttendance.getAttendanceStatus())) {
								dataList.add("A");
								inTimeList.add(ApplicationConstants.DELIMITER_HYPHEN);
								outTimeList.add(ApplicationConstants.DELIMITER_HYPHEN);
							}
							else if(NMDCConstants.PRESENT.equalsIgnoreCase(dailyAttendance.getAttendanceStatus())) {
								dataList.add("P");
								inTimeList.add(dailyAttendance.getEmpInTime());
								outTimeList.add(dailyAttendance.getEmpOutTime());
							}
								
							else if(null == dailyAttendance.getAttendanceStatus()) {
								dataList.add(ApplicationConstants.DELIMITER_HYPHEN);
								inTimeList.add(ApplicationConstants.DELIMITER_HYPHEN);
								outTimeList.add(ApplicationConstants.DELIMITER_HYPHEN);
							}
								
							if (dailyReportListItr.hasNext()) {
								dailyAttendance = dailyReportListItr.next();
							}
						}else {
							dataList.add(ApplicationConstants.DELIMITER_HYPHEN);
							inTimeList.add(ApplicationConstants.DELIMITER_HYPHEN);
							outTimeList.add(ApplicationConstants.DELIMITER_HYPHEN);
						}
					}else{
						dataList.add(ApplicationConstants.DELIMITER_HYPHEN);
						inTimeList.add(ApplicationConstants.DELIMITER_HYPHEN);
						outTimeList.add(ApplicationConstants.DELIMITER_HYPHEN);
					}
					
					first++;
				}
				
				monthlyDailyReportDto.setTotalPresentCount(String.valueOf(totalPresentCount));
				monthlyDailyReportDto.setTotalOverTime(String.valueOf(totalOverTime/60)+":"+String.valueOf(totalOverTime%60));
				
				monthlyDailyReportDto.setTotalDays(String.valueOf(lastDayOfMonth));
				
				monthlyDailyReportDto.setDateList(dataList);
				monthlyDailyReportDto.setInTimeList(inTimeList);
				monthlyDailyReportDto.setOutTimeList(outTimeList);
			}
			
			private List<String> getHeadListWithInOutTime(String month, int day, int last) {
				List<String> headList = new ArrayList<String>();
				headList.add(NMDCConstants.EMPLOYEE_ID);
				headList.add(NMDCConstants.NAME);
				headList.add(HeaderConstants.COMPANY);
				headList.add(HeaderConstants.DEPARTMENT);
				headList.add(HeaderConstants.DESIGNATION);
				headList.add(HeaderConstants.GRADE);
				headList.add(HeaderConstants.MOBILE_NO);
				while(day <= last) {
					headList.add("In Time");
					headList.add("Out Time");
					headList.add(day+ApplicationConstants.DELIMITER_SPACE+month.substring(NumberConstants.ZERO, NumberConstants.THREE));
					day++;
				}
				headList.add(ApplicationConstants.TOTAL_PRESENT);
				headList.add(NMDCConstants.TOTAL_OVERTIME);
				return headList;
			}
			
			public void excelGeneratorWithInOutTime(HttpServletResponse response, NMDCReportDto<NMDCMonthlyAttendanceDto> monthlyDetailsList)
					throws ParseException, IOException {

				DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_INDIA_SPLIT_BY_UNDERSCORE);
				String currentDateTime = dateFormat.format(new Date());
				String filename = "In_&_Out_Monthly_Attendance_" + currentDateTime + ApplicationConstants.EXTENSION_EXCEL;
				Workbook workBook = new XSSFWorkbook();
				Sheet sheet = workBook.createSheet();
			
				int rowCount = NumberConstants.ZERO;
				Row row = sheet.createRow(rowCount++);
			
				Font font = workBook.createFont();
				font.setBold(true);
			
				//set border style for header data
				CellStyle cellStyle = setExcelBorderStyle(workBook, BorderStyle.THICK, font);
			
				int index=NumberConstants.ZERO;
				Cell cell = row.createCell(NumberConstants.ZERO);
				List<String> headList = monthlyDetailsList.getHeadList();
				for(String head : headList) {
					cell = row.createCell(index++);
					cell.setCellValue(head);
					cell.setCellStyle(cellStyle);
				}
				
				
				font = workBook.createFont();
				font.setBold(false);
			
				//set border style for body data
				cellStyle = setExcelBorderStyle(workBook, BorderStyle.THIN, font);
			
				List<NMDCMonthlyAttendanceDto> incapMonthlyDetailDtoList = monthlyDetailsList.getDataList();
				//set excel data for incap monthly report
				setExcelDataForMonthlyReportWithInOutTime(sheet, rowCount, cellStyle, incapMonthlyDetailDtoList);
			
				FileOutputStream fileOut = new FileOutputStream(filename);
				workBook.write(fileOut);
				ServletOutputStream outputStream = response.getOutputStream();
				workBook.write(outputStream);
				fileOut.close();
				workBook.close();

			}
			
			private void setExcelDataForMonthlyReportWithInOutTime(Sheet sheet, int rowCount, CellStyle cellStyle,
					List<NMDCMonthlyAttendanceDto> incapMonthlyDetailDtoList) {
				
				for (NMDCMonthlyAttendanceDto monthlyDetail : incapMonthlyDetailDtoList) {
					if(rowCount==90000)
						break;
					Row row = sheet.createRow(rowCount++);

					int columnCount = NumberConstants.ZERO;

					Cell cell = row.createCell(columnCount++);
					cell.setCellValue(monthlyDetail.getEmpId());
					cell.setCellStyle(cellStyle);

					cell = row.createCell(columnCount++);
					cell.setCellValue(monthlyDetail.getEmpName());
					cell.setCellStyle(cellStyle);
					
					cell = row.createCell(columnCount++);
					cell.setCellValue(monthlyDetail.getCompany());
					cell.setCellStyle(cellStyle);
					
					cell = row.createCell(columnCount++);
					cell.setCellValue(monthlyDetail.getDepartment());
					cell.setCellStyle(cellStyle);
					
					cell = row.createCell(columnCount++);
					cell.setCellValue(monthlyDetail.getDesignation());
					cell.setCellStyle(cellStyle);
					
					cell = row.createCell(columnCount++);
					cell.setCellValue(monthlyDetail.getGrade());
					cell.setCellStyle(cellStyle);
					
					cell = row.createCell(columnCount++);
					cell.setCellValue(monthlyDetail.getMobile());
					cell.setCellStyle(cellStyle);
					
					//month
					for(int i=0; i<monthlyDetail.getDateList().size(); i++) {
						cell = row.createCell(columnCount++);
						cell.setCellValue(monthlyDetail.getInTimeList().get(i));
						cell.setCellStyle(cellStyle);
						
						cell = row.createCell(columnCount++);
						cell.setCellValue(monthlyDetail.getOutTimeList().get(i));
						cell.setCellStyle(cellStyle);
						
						cell = row.createCell(columnCount++);
						cell.setCellValue(monthlyDetail.getDateList().get(i));
						cell.setCellStyle(cellStyle);
					}

					cell = row.createCell(columnCount++);
					cell.setCellValue(monthlyDetail.getTotalPresentCount());
					cell.setCellStyle(cellStyle);
					
					cell = row.createCell(columnCount++);
					cell.setCellValue(monthlyDetail.getTotalOverTime());
					cell.setCellStyle(cellStyle);
				
				}
			}


			public PaginationDto<NMDCReportDto<NMDCMonthlyAttendanceDto>> searchInOut(String dateStr, String employeeId,
					String employeeName, String department, String designation,String company, String organization, int pageno, String sortField, String sortDir) {


				PaginationDto<NMDCReportDto<NMDCMonthlyAttendanceDto>> dtoList = new PaginationDto<>();
				NMDCReportDto<NMDCMonthlyAttendanceDto> monthlyDetailsReport = new NMDCReportDto<>();
				Date date = null;
				SimpleDateFormat format = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US);
				if(dateStr.isEmpty()) {
					dateStr = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US).format(new Date());
				}
				else {
					dateStr=dateStr+"-01";
				}
					try {

						date = format.parse(dateStr);
						Calendar dateCalendar = Calendar.getInstance(); 
						dateCalendar.setTime(date);
						String month = dateCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH );
						
						int first = dateCalendar.getActualMinimum(Calendar.DATE), day = dateCalendar.getActualMinimum(Calendar.DATE);
						int last = dateCalendar.getActualMaximum(Calendar.DATE);
						
						List<String> headList = getHeadListWithInOutTime(month, day, last);

						Date startCalendar = calendarUtil.getConvertedDate(date, first, NumberConstants.ZERO, NumberConstants.ZERO, NumberConstants.ZERO);
						
						Date endCalendar = calendarUtil.getConvertedDate(date,last, NumberConstants.TWENTY_THREE, NumberConstants.FIFTY_NINE, NumberConstants.FIFTY_NINE);
						
						if (null == sortDir || sortDir.isEmpty()) {
							sortDir = ApplicationConstants.ASC;
						}
						if (null == sortField || sortField.isEmpty()) {
							sortField = ApplicationConstants.ID;
						}
						Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
								: Sort.by(sortField).descending();

						Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
						
				    	
				    	Specification<Employee> isDeletedFalse = generalSpecificationEmployee.isDeletedSpecification();
						Specification<Employee> nameSpc = generalSpecificationEmployee.stringSpecification(employeeName, ApplicationConstants.NAME);
						Specification<Employee> empIdSpc = generalSpecificationEmployee.stringSpecification(employeeId, EmployeeConstants.EMPID);
						Specification<Employee> deptSpec = generalSpecificationEmployee.foreignKeyStringSpecification(department, EmployeeConstants.DEPARTMENT,ApplicationConstants.NAME);
						Specification<Employee> designationSpc = generalSpecificationEmployee.foreignKeyStringSpecification(designation, EmployeeConstants.DESIGNATION,ApplicationConstants.NAME);
						Specification<Employee> companySpc = generalSpecificationEmployee.stringSpecification(company, DailyAttendanceConstants.COMPANY);
						Specification<Employee> orgSpc = generalSpecificationEmployee.foreignKeyStringSpecification(organization, AreaConstants.ORGANIZATION, ApplicationConstants.NAME);
						Page<Employee> page = employeeRepository.findAll(isDeletedFalse.and(designationSpc).and(empIdSpc).and(companySpc).and(nameSpc).and(deptSpec).and(orgSpc),pageable);
						
						setInOutMonthlyAttendanceDtoList(monthlyDetailsReport, headList, startCalendar, endCalendar, page);

						sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir)) ? ApplicationConstants.DESC : ApplicationConstants.ASC;
						List<NMDCReportDto<NMDCMonthlyAttendanceDto>> monthlyDetailsReportList = new ArrayList<NMDCReportDto<NMDCMonthlyAttendanceDto>>();
						monthlyDetailsReportList.add(monthlyDetailsReport);
						dtoList = new PaginationDto<>(monthlyDetailsReportList, page.getTotalPages(), page.getNumber() + NumberConstants.ONE,
								page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);

					} catch (ParseException e) {
						e.printStackTrace();
					}
				return dtoList;
			
			}
			
			private void setInOutMonthlyAttendanceDtoList(NMDCReportDto<NMDCMonthlyAttendanceDto> monthlyDetailsReport,
					List<String> headList, Date startCalendar, Date endCalendar, Page<Employee> page) {
				List<Employee> workerList = page.getContent();
				List<NMDCMonthlyAttendanceDto> monthlyReportList = new ArrayList<>();
				for (Employee employee : workerList) {
					NMDCMonthlyAttendanceDto monthlyDetailDto = calculateDaywiseMonthlyReportWithInOutTime(startCalendar, endCalendar, employee);
					monthlyReportList.add(monthlyDetailDto);
				}

				monthlyDetailsReport.setHeadList(headList);
				monthlyDetailsReport.setDataList(monthlyReportList);
			}
}
