package com.eikona.tech.repository;


import java.util.List;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.eikona.tech.entity.Employee;
import com.eikona.tech.entity.Organization;


@Repository
public interface EmployeeRepository extends DataTablesRepository<Employee, Long> {

	List<Employee> findAllByIsDeletedFalse();

	@Query("select emp.id from com.eikona.tech.entity.Employee as emp where emp.isDeleted=false and empId=:empId")
	Long findByEmpIdAndIsDeletedFalseCustom(String empId);
	
	Employee findByEmpIdAndIsDeletedFalse(String empId);
	
	@Query("select emp.empId from com.eikona.tech.entity.Employee as emp where emp.isDeleted=false")
	List<String> getEmpIdAndIsDeletedFalseCustom();


	List<Employee> findByOrganizationAndIsDeletedFalse(Organization org);

	List<Employee> findAllByIsDeletedTrueAndIsSyncTrue();

	Employee findByNameAndIsDeletedFalse(String name);
	
	Employee findByEmpId(String string);
	
	@Query("Select e.id from com.eikona.tech.entity.Employee e where e.isDeleted=false")
	List<Long> employeeIdListCustom();
	
	@Query("Select e from com.eikona.tech.entity.Employee e where e.isDeleted=false and e.organization.name=:org")
	List<Employee> findAllByIsDeletedFalseAndOrganization(String org);

	Employee findByDeviceEmpIdAndIsDeletedFalse(String empId);

	@Query("Select e from com.eikona.tech.entity.Employee e where e.isDeleted=false and e.empId like %:empId")
	Employee findByEmpIdCustom(String empId);

	Employee findByDeviceEmpIdAndOrganizationAndIsDeletedFalse(String empId, Organization organization);

	List<Employee> findAllByIsDeletedFalseAndOrganization(Organization organization);

	@Query("select e from com.eikona.tech.entity.Employee as e where e.isDeleted=false and e.empId NOT IN :empIds and e.organization.name=:organization")
	List<Employee> findByEmpIdAndIsDeletedFalseCustom(List<String> empIds, String organization);

	List<Employee> findAllByOrganization(Organization organization);
	
}