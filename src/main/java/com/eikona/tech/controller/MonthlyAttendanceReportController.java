package com.eikona.tech.controller;

import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eikona.tech.dto.NMDCMonthlyAttendanceDto;
import com.eikona.tech.dto.NMDCReportDto;
import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.User;
import com.eikona.tech.repository.UserRepository;
import com.eikona.tech.service.impl.model.NMDCMonthlyAttendanceReportServiceImpl;


@Controller
public class MonthlyAttendanceReportController {
	
	@Autowired
	private NMDCMonthlyAttendanceReportServiceImpl nmdcMonthlyAttendanceReportServiceImpl;
	
	@Autowired
	private UserRepository userRepository;
	
	@GetMapping("/monthly-attendance/report")
	@PreAuthorize("hasAuthority('monthlyattendance_view')")
	public String attendanceReportPage() {
		return "NMDCReport/monthlyAttendanceReport";
	}
	
	@GetMapping("/in-out/monthly-attendance/report")
	@PreAuthorize("hasAuthority('monthlyattendance_view')")
	public String inOutAttendanceReportPage() {
		return "NMDCReport/inOutMonthlyAttendanceReport";
	}
	
	@GetMapping("/api/monthly/attendance/export-to-file")
	@PreAuthorize("hasAuthority('monthlyattendance_export')")
	public void excelGenerateIncapAttendance(HttpServletResponse response, String date,String employeeId,String employeeName,String designation,String department,String company,String flag,Principal principal) {
		response.setContentType("application/octet-stream");
		DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
		String currentDateTime = dateFormat.format(new Date());
		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=Monthly_Attendance_" + currentDateTime + "."+flag;
		response.setHeader(headerKey, headerValue);
		
		try {
			User user = userRepository.findByUserNameAndIsDeletedFalse(principal.getName());
			String orgname = (null == user.getOrganization()?null:user.getOrganization().getName());
			NMDCReportDto<NMDCMonthlyAttendanceDto> monthlyDataList = nmdcMonthlyAttendanceReportServiceImpl.calculateMonthlyReport(date,employeeId,employeeName,department,designation,company,orgname);
			nmdcMonthlyAttendanceReportServiceImpl.excelGenerator(response, monthlyDataList);
			
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value = "/api/search/monthly-attendance-report", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('monthlyattendance_view')")
	public @ResponseBody PaginationDto<NMDCReportDto<NMDCMonthlyAttendanceDto>> searchMonthlyReport(String date,String employeeId,String employeeName,String department,String designation,String company, int pageno, String sortField, String sortDir,Principal principal) {
		User user = userRepository.findByUserNameAndIsDeletedFalse(principal.getName());
		String orgname = (null == user.getOrganization()?null:user.getOrganization().getName());
		PaginationDto<NMDCReportDto<NMDCMonthlyAttendanceDto>> dtoList = nmdcMonthlyAttendanceReportServiceImpl.search(date,employeeId,employeeName,department,designation,company, pageno, sortField, sortDir,orgname);
		
		return dtoList;
	}
	
	//Employee Monthly Report With In Out Time 
			@GetMapping("/api/in-out/monthly-attendance/export-to-file")
			@PreAuthorize("hasAuthority('monthlyattendance_export')")
			public void excelGenerateIncapAttendance1(HttpServletResponse response, String date,String employeeId,String employeeName,String designation,String department,String company,String flag, Principal principal) {
				
				User user = userRepository.findByUserNameAndIsDeletedFalse(principal.getName());
				String orgname = (null == user.getOrganization()?null:user.getOrganization().getName());response.setContentType("application/octet-stream");
				DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
				String currentDateTime = dateFormat.format(new Date());
				String headerKey = "Content-Disposition";
				String headerValue = "attachment; filename=Monthly_Attendance_" + currentDateTime + "."+flag;
				response.setHeader(headerKey, headerValue);
				
				try {
					
					NMDCReportDto<NMDCMonthlyAttendanceDto> monthlyDataList = nmdcMonthlyAttendanceReportServiceImpl.calculateMonthlyReportWithInOutTime(date,employeeId,employeeName,department,designation,company, orgname);
					nmdcMonthlyAttendanceReportServiceImpl.excelGeneratorWithInOutTime(response, monthlyDataList);
					
				} catch (Exception  e) {
					e.printStackTrace();
				}
			}
			
			@RequestMapping(value = "/api/search/in-out/monthly-attendance-report", method = RequestMethod.GET)
			@PreAuthorize("hasAuthority('monthlyattendance_view')")
			public @ResponseBody PaginationDto<NMDCReportDto<NMDCMonthlyAttendanceDto>> searchInOutMonthlyReport(String date,String employeeId,String employeeName,String department,String designation,String company,int pageno, String sortField, String sortDir, Principal principal) {
				
				User user = userRepository.findByUserNameAndIsDeletedFalse(principal.getName());
				String orgname = (null == user.getOrganization()?null:user.getOrganization().getName());
				PaginationDto<NMDCReportDto<NMDCMonthlyAttendanceDto>> dtoList = nmdcMonthlyAttendanceReportServiceImpl.searchInOut(date,employeeId,employeeName,department,designation,company, orgname, pageno, sortField, sortDir);
				
				return dtoList;
			}
}
