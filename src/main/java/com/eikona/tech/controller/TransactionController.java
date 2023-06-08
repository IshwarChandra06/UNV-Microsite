package com.eikona.tech.controller;

import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.entity.Transaction;
import com.eikona.tech.entity.User;
import com.eikona.tech.repository.EmployeeRepository;
import com.eikona.tech.repository.TransactionRepository;
import com.eikona.tech.repository.UserRepository;
import com.eikona.tech.service.DepartmentService;
import com.eikona.tech.service.DesignationService;
import com.eikona.tech.service.OrganizationService;
import com.eikona.tech.service.TransactionService;
import com.eikona.tech.service.impl.model.TransactionServiceImpl;
import com.eikona.tech.util.ExportUnenrolledEmployee;
import com.eikona.tech.util.ImageProcessingUtil;

@Controller
public class TransactionController {
	
	@Autowired
	private TransactionService transactionService;
	
	@Autowired
	private ImageProcessingUtil imageProcessingUtil;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private TransactionServiceImpl transactionserviceImpl;
	
	@Autowired
	private OrganizationService organizationService;
	
	@Autowired
	private DepartmentService departmentService;
	
	@Autowired
	private DesignationService designationService;
	
	@Autowired
	private TransactionRepository transactionRepository;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private ExportUnenrolledEmployee exportUnenrolled;
	
	@GetMapping("/transaction")
	@PreAuthorize("hasAuthority('transaction_view')")
	public String transactionList() {
		return "transaction/transaction_list";
	}
	@GetMapping("/unenrolled-employee")
	@PreAuthorize("hasAuthority('transaction_view')")
	public String unenrolledTransactionList() {
		return "transaction/unenrolled_transaction";
	}
	@GetMapping("/unregistered-logs")
	@PreAuthorize("hasAuthority('transaction_view')")
	public String unregisteredTransactionList() {
		return "transaction/unregistered_transaction";
	}
	
	@GetMapping("/update-events")
	public String updateEvents() {
		List<Transaction> transList=(List<Transaction>) transactionRepository.findAll();
		List<Transaction> saveList = new ArrayList<>();
		for(Transaction tran:transList) {
			if(null==tran.getOrganization() || tran.getOrganization().isEmpty()) {
				Employee employee=employeeRepository.findByDeviceEmpIdAndIsDeletedFalse(tran.getEmployeeCode());
				
				if(null!=employee) {
					if(null!=employee.getDepartment())
					 tran.setDepartment(employee.getDepartment().getName());
					if(null!=employee.getDesignation())
					 tran.setDesignation(employee.getDesignation().getName());
					tran.setEmpId(employee.getEmpId());
					tran.setGrade(employee.getGrade());
					tran.setOrganization(employee.getOrganization().getName());
					saveList.add(tran);
				}
			}
		}
		transactionRepository.saveAll(saveList);
		return "transaction/transaction_list";
	}
	@GetMapping("/add/employee-from-transaction/{id}")
	@PreAuthorize("hasAuthority('transaction_view')")
	public String editEmployee(@PathVariable(value = "id") long id, Model model, Principal principal) {
		
		User user = userRepository.findByUserNameAndIsDeletedFalse(principal.getName());

		model.addAttribute("listOrganization", organizationService.getById(user.getOrganization().getId()));
		model.addAttribute("listDepartment", departmentService.getAllByOrganization(user.getOrganization()));
		model.addAttribute("listDesignation", designationService.getAllByOrganization(user.getOrganization()));
		
		Transaction trans= transactionRepository.findById(id).get();

		Employee employee = new Employee();
		employee.setDeviceEmpId(trans.getEmployeeCode());
		employee.setName(trans.getName());
		
		model.addAttribute("employee", employee);
		model.addAttribute("title", "New Employee");
		return "employee/employee_new";
	}

	//time log
	@GetMapping("/upload/timelog-transaction")
	public String indexTimeLog() {
		return "multipartfile/upload_timelog";
	}

	@PostMapping("/import/timelog-transaction/excel")
	public String uploadMultipartFileTimeLog(@RequestParam("uploadfile") MultipartFile file, Model model) {
		try {
			transactionserviceImpl.storeTimeLog(file);
			model.addAttribute("message", "File uploaded successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("message", "Fail! -> uploaded filename: " + file.getOriginalFilename());
		}
		return "multipartfile/upload_timelog";
	}
	//search data
	@RequestMapping(value = "/api/search/transaction", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('transaction_view')")
	public @ResponseBody PaginationDto<Transaction> search(String sDate,String eDate, String employeeId, String employeeCode, String employeeName, String device, String department, String designation,String company,
			int pageno, String sortField, String sortDir, Principal principal) {
		
		User user = userRepository.findByUserNameAndIsDeletedFalse(principal.getName());
		String orgName = (null == user.getOrganization()?null : user.getOrganization().getName());
		PaginationDto<Transaction> dtoList = transactionService.searchByField(sDate, eDate, employeeId, employeeCode, employeeName, device, department, designation,company, pageno, sortField, sortDir, orgName);
		
		setTransactionImage(dtoList);
		return dtoList;
	}
	
	@RequestMapping(value = "/api/search/unenrolled-transaction", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('transaction_view')")
	public @ResponseBody PaginationDto<Transaction> searchUnenrolled(
			int pageno, String sortField, String sortDir, Principal principal) {
		
		User user = userRepository.findByUserNameAndIsDeletedFalse(principal.getName());
		String orgName = (null == user.getOrganization()?null : user.getOrganization().getName());
		PaginationDto<Transaction> dtoList = transactionService.searchByField(pageno, sortField, sortDir, orgName,"Unenrolled");
		
		setTransactionImage(dtoList);
		return dtoList;
	}
	@RequestMapping(value = "/api/search/unregistered-transaction", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('transaction_view')")
	public @ResponseBody PaginationDto<Transaction> searchUnregistered(int pageno, String sortField, String sortDir, Principal principal) {
		
		User user = userRepository.findByUserNameAndIsDeletedFalse(principal.getName());
		String orgName = (null == user.getOrganization()?null : user.getOrganization().getName());
		PaginationDto<Transaction> dtoList = transactionService.searchByField(pageno, sortField, sortDir, orgName,"Unregistered");
		
		setTransactionImage(dtoList);
		return dtoList;
	}
	
	@RequestMapping(value="/api/unenrolled-employee/export-to-excel",method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('employee_export')")
	public void exportToFile(HttpServletResponse response, String flag, Principal principal) {
		User userObj = userRepository.findByUserNameAndIsDeletedFalse(principal.getName());
		String orgName = (null == userObj.getOrganization()? null : userObj.getOrganization().getName());
		 response.setContentType("application/octet-stream");
			DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
			String currentDateTime = dateFormat.format(new Date());
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=Employee_master_data" + currentDateTime + "."+flag;
			response.setHeader(headerKey, headerValue);
		try {
			exportUnenrolled.fileExportBySearchValue(response, flag, orgName );
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}
	
	private void setTransactionImage(PaginationDto<Transaction> dtoList) {
		List<Transaction> eventsList = dtoList.getData();
		List<Transaction> transactionList = new ArrayList<Transaction>();
		for (Transaction trans : eventsList) {
			byte[] image = imageProcessingUtil.searchTransactionImage(trans);
			trans.setCropImageByte(image);
			transactionList.add(trans);
		}
		dtoList.setData(transactionList);
	}
}
