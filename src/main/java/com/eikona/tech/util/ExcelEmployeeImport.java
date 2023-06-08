package com.eikona.tech.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.DefaultConstants;
import com.eikona.tech.constants.MessageConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.entity.Department;
import com.eikona.tech.entity.Designation;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.entity.Organization;
import com.eikona.tech.entity.User;
import com.eikona.tech.repository.DepartmentRepository;
import com.eikona.tech.repository.DesignationRepository;
import com.eikona.tech.repository.EmployeeRepository;
import com.eikona.tech.repository.OrganizationRepository;
import com.eikona.tech.repository.UserRepository;

@Component
public class ExcelEmployeeImport {

	@Autowired
	private DepartmentRepository departmentrepository;

	@Autowired
	private DesignationRepository designationrepository;

	@Autowired
	private OrganizationRepository organizationrepository;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private EmployeeObjectMap employeeObjectMap;
	
	@Autowired
	private UserRepository userRepository;

	public Employee excelRowToEmployee(Row currentRow, Organization org, Map<String, Designation> designationMap,
			Map<String, Department> deptMap) throws ParseException {

		Employee employeeObj = null;

		Iterator<Cell> cellsInRow = currentRow.iterator();
		int cellIndex = NumberConstants.ZERO;
		employeeObj = new Employee();

		while (cellsInRow.hasNext()) {
			Cell currentCell = cellsInRow.next();
			cellIndex = currentCell.getColumnIndex();
			if (null == employeeObj) {
				break;
			}

			else if (cellIndex == NumberConstants.ZERO) {
				String value = getStringValue(currentCell);
				employeeObj.setName(value);
				employeeObj.setOrganization(org);
			} else if (cellIndex == NumberConstants.ONE) {
				String value = getStringValue(currentCell);
				employeeObj.setMother(value);
			} else if (cellIndex == NumberConstants.TWO) {
				String value = getStringValue(currentCell);
				employeeObj.setMobile(value);
			} else if (cellIndex == NumberConstants.THREE) {
				String value = getStringValue(currentCell);
				employeeObj.setBloodGroup(value);
			} else if (cellIndex == NumberConstants.FOUR) {
				setDob(employeeObj, currentCell);
			} else if (cellIndex == NumberConstants.FIVE) {
				String value = getStringValue(currentCell);
				employeeObj.setGender(value);
			} else if (cellIndex == NumberConstants.SIX) {
				String value = getStringValue(currentCell);
				employeeObj.setCompany(value);
			} else if (cellIndex == NumberConstants.SEVEN) {
				setDepartment(org, deptMap, employeeObj, currentCell);
			} else if (cellIndex == NumberConstants.EIGHT) {
				setDesignation(org, designationMap, employeeObj, currentCell);
			} else if (cellIndex == NumberConstants.NINE) {
				String value = getStringValue(currentCell);
				employeeObj.setGrade(value);
			} else if (cellIndex == NumberConstants.TEN) {
				String value = getStringValue(currentCell);
				employeeObj.setCardNo(value);
			} else if (cellIndex == NumberConstants.ELEVEN) {
				String value = getStringValue(currentCell);
				employeeObj.setCity(value);
			} else if (cellIndex == NumberConstants.TWELVE) {
				String value = getStringValue(currentCell);
				employeeObj.setEmpId(value);
			} else if (cellIndex == NumberConstants.THIRTEEN) {
				String value = getStringValue(currentCell);
				employeeObj.setDeviceEmpId(value);
			} else if (cellIndex == NumberConstants.FOURTEEN) {
				String value = getStringValue(currentCell);
				employeeObj.setFather(value);
			} else if (cellIndex == NumberConstants.FIFTEEN) {
				String value = getStringValue(currentCell);
				employeeObj.setEmailOfficial(value);
			} else if (cellIndex == NumberConstants.SIXTEEN) {
				String value = getStringValue(currentCell);
				employeeObj.setEmailPersonal(value);
			} else if (cellIndex == NumberConstants.SEVENTEEN) {
				String value = getStringValue(currentCell);
				employeeObj.setPermanentAddress(value);
			} else if (cellIndex == NumberConstants.EIGHTEEN) {
				String value = getStringValue(currentCell);
				employeeObj.setResidentialAddress(value);
			} else if (cellIndex == NumberConstants.NINETEEN) {
				setJoinDate(employeeObj, currentCell);
			} else if (cellIndex == NumberConstants.TWENTY) {
				String value = getStringValue(currentCell);
				employeeObj.setBranch(value);
			}

		}
		return employeeObj;

	}

	private void setDesignation(Organization org, Map<String, Designation> designationMap, Employee employeeObj,
			Cell currentCell) {
		String str = getStringValue(currentCell);
		if (null != str && !str.isEmpty()) {

			Designation designation = designationMap.get(str);
			if (null == designation) {
				designation = new Designation();
				designation.setName(str);
				designation.setOrganization(org);
				designationrepository.save(designation);
				designationMap.put(designation.getName(), designation);

			}
			employeeObj.setDesignation(designation);
		}
	}

	private void setDepartment(Organization org, Map<String, Department> deptMap, Employee employeeObj,
			Cell currentCell) {
		String str = getStringValue(currentCell);

		if (null != str && !str.isEmpty()) {

			Department department = deptMap.get(str);
			if (null == department) {
				department = new Department();
				department.setName(str);
				department.setOrganization(org);
				departmentrepository.save(department);
				deptMap.put(department.getName(), department);
			}
			employeeObj.setDepartment(department);
		}
	}

	@SuppressWarnings(ApplicationConstants.DEPRECATION)
	private String getStringValue(Cell currentCell) {
		currentCell.setCellType(CellType.STRING);
		String value = "";
		if (currentCell.getCellType() == CellType.NUMERIC) {
			value = String.valueOf(currentCell.getNumericCellValue());
		} else if (currentCell.getCellType() == CellType.STRING) {
			value = currentCell.getStringCellValue();
		}
		return value;
	}
	
	private void setDob(Employee employee,Cell currentCell) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		Date date =null;
		 if (currentCell.getCellType() == CellType.STRING) {
			 date=inputFormat.parse(currentCell.getStringCellValue().trim());
			 if(null!=date)
			 employee.setDob(dateFormat.format(date));
		 }
		 else if (currentCell.getCellType() == CellType.NUMERIC) {
	           date = currentCell.getDateCellValue();
	           String dateStr = dateFormat.format(date);
	           employee.setDob(dateStr);
			}
		 else {
			 if(!currentCell.getStringCellValue().isEmpty()) {
				 date = (Date)currentCell.getDateCellValue();
				 if(null!=date)
				 employee.setDob(dateFormat.format(date));
			 }
			
		 }
	}
	
	private void setJoinDate(Employee employee,Cell currentCell) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		Date date =null;
		 if (currentCell.getCellType() == CellType.STRING) {
			 date=inputFormat.parse(currentCell.getStringCellValue().trim());
			 if(null!=date)
			 employee.setJoinDate(dateFormat.format(date));
		 }
		  else if (currentCell.getCellType() == CellType.NUMERIC) {
			  date = currentCell.getDateCellValue();
	           String dateStr = dateFormat.format(date);
	           employee.setJoinDate(dateStr);
			} 
		  else {
			  if(!currentCell.getStringCellValue().isEmpty()) {
				  date = (Date)currentCell.getDateCellValue();
					 if(null!=date)
					 employee.setJoinDate(dateFormat.format(date));
			  }
			
		 }
	}

	public List<Employee> parseExcelFileEmployeeList(InputStream inputStream, Principal principal) throws ParseException {
		List<Employee> employeeList = new ArrayList<Employee>();
		try {

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheetAt(NumberConstants.ZERO);

			Iterator<Row> rows = sheet.iterator();

			int rowNumber = NumberConstants.ZERO;
			Map<String, Designation> designationMap = employeeObjectMap.getDesignation();
			Map<String, Department> deptMap = employeeObjectMap.getDepartment();
//			List<String> empIdList = employeeRepository.getEmpIdAndIsDeletedFalseCustom();
			
			User user = userRepository.findByUserNameAndIsDeletedFalse(principal.getName());
			Map<String, Employee> employeeMap = employeeObjectMap.getEmployeeByEmpId(user.getOrganization());
			Organization org = user.getOrganization();
			while (rows.hasNext()) {
				Row currentRow = rows.next();

				// skip header
				if (rowNumber == NumberConstants.ZERO) {
					rowNumber++;
					continue;
				}

				rowNumber++;

				Employee employee = excelRowToEmployee(currentRow, org, designationMap, deptMap);

//				boolean isContains = empIdList.contains(employee.getEmpId());
                Employee emp=employeeMap.get(employee.getEmpId());
				
				if(null==emp && null != employee.getName() && !employee.getName().isEmpty()
						&& null != employee.getEmpId() && !employee.getEmpId().isEmpty())
					employeeList.add(employee);
				else if(null!=emp) {
					emp.setFather(employee.getFather());
					emp.setMother(employee.getMother());
					emp.setDob(employee.getDob());
					emp.setBloodGroup(employee.getBloodGroup());
					emp.setBranch(employee.getBranch());
					emp.setCardNo(employee.getCardNo());
					emp.setCity(employee.getCity());
					emp.setCompany(employee.getCompany());
					emp.setDeviceEmpId(employee.getDeviceEmpId());
					emp.setName(employee.getName());
					emp.setGender(employee.getGender());
					emp.setEmailOfficial(employee.getEmailOfficial());
					emp.setEmailPersonal(employee.getEmailPersonal());
					emp.setDepartment(employee.getDepartment());
					emp.setDesignation(employee.getDesignation());
					emp.setGrade(employee.getGrade());
					emp.setMobile(employee.getMobile());
					emp.setPermanentAddress(employee.getPermanentAddress());
					emp.setResidentialAddress(employee.getResidentialAddress());
					emp.setJoinDate(employee.getJoinDate());
					emp.setDeleted(false);
					employeeList.add(emp);
				}
				if (rowNumber % NumberConstants.HUNDRED == NumberConstants.ZERO) {
					employeeRepository.saveAll(employeeList);
					employeeList.clear();
				}

			}

			if (!employeeList.isEmpty()) {
				employeeRepository.saveAll(employeeList);
				employeeList.clear();
			}

			workbook.close();

			return employeeList;
		} catch (IOException e) {
			throw new RuntimeException(MessageConstants.FAILED_MESSAGE + e.getMessage());
		}
	}

	public Employee cosecExcelRowToEmployee(Row currentRow, Organization org) {

		Employee employeeObj = null;

		Iterator<Cell> cellsInRow = currentRow.iterator();
		int cellIndex = NumberConstants.ZERO;
		employeeObj = new Employee();
		while (cellsInRow.hasNext()) {
			Cell currentCell = cellsInRow.next();

			if (null == employeeObj) {
				break;
			}

			else if (cellIndex == NumberConstants.ZERO) {
				String value=getStringValue(currentCell);
				employeeObj.setEmpId(value);
			} else if (cellIndex == NumberConstants.TWO) {
				employeeObj.setName(currentCell.getStringCellValue());
				employeeObj.setOrganization(org);
			}

			cellIndex++;
		}
		return employeeObj;

	}

	public List<Employee> parseCosecExcelFileEmployeeList(InputStream inputStream) throws InvalidFormatException {
		List<Employee> employeeList = new ArrayList<Employee>();
		try {
			// OPCPackage pkg = OPCPackage.open(inputStream);

			Workbook workbook = new HSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheetAt(NumberConstants.ZERO);

			Iterator<Row> rows = sheet.iterator();

			int rowNumber = NumberConstants.ZERO;
			List<String> empIdList = employeeRepository.getEmpIdAndIsDeletedFalseCustom();
			Organization org = organizationrepository.findById(DefaultConstants.DEFAULT_ORGANIZATION_ID).get();
			while (rows.hasNext()) {
				Row currentRow = rows.next();

				// skip header
				if (rowNumber == NumberConstants.ZERO) {
					rowNumber++;
					continue;
				}

				rowNumber++;

				Employee employee = cosecExcelRowToEmployee(currentRow, org);

				boolean isContains = empIdList.contains(employee.getEmpId());

				if (!isContains && null != employee.getName() && !employee.getName().isEmpty()
						&& null != employee.getEmpId() && !employee.getEmpId().isEmpty())
					employeeList.add(employee);

				if (rowNumber % NumberConstants.HUNDRED == NumberConstants.ZERO) {
					employeeRepository.saveAll(employeeList);
					employeeList.clear();
				}

			}

			if (!employeeList.isEmpty()) {
				employeeRepository.saveAll(employeeList);
				employeeList.clear();
			}

			workbook.close();

			return employeeList;
		} catch (IOException e) {
			throw new RuntimeException(MessageConstants.FAILED_MESSAGE + e.getMessage());
		}
	}


	public void parseEmployeeListForDelete(InputStream inputStream) {

		try {

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheetAt(NumberConstants.ZERO);

			Iterator<Row> rows = sheet.iterator();

			int rowNumber = NumberConstants.ZERO;
			while (rows.hasNext()) {
				Row currentRow = rows.next();

				// skip header
				if (rowNumber == NumberConstants.ZERO) {
					rowNumber++;
					continue;
				}

				rowNumber++;

				Iterator<Cell> cellsInRow = currentRow.iterator();
				int cellIndex = NumberConstants.ZERO;

				while (cellsInRow.hasNext()) {
					Cell currentCell = cellsInRow.next();
					cellIndex = currentCell.getColumnIndex();

					 if (cellIndex == NumberConstants.ZERO) {
						String value = getStringValue(currentCell);
						Employee employee = employeeRepository.findByEmpIdAndIsDeletedFalse(value.trim());
						if (null!=employee) {
							employee.setDeleted(true);
							employeeRepository.save(employee);
						}
					}

			}


			workbook.close();

		}
		}catch (IOException e) {
			throw new RuntimeException(MessageConstants.FAILED_MESSAGE + e.getMessage());
		}
	
	}

}
