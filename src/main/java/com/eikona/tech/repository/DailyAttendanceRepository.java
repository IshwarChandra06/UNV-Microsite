package com.eikona.tech.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.eikona.tech.entity.DailyReport;

@Repository
public interface DailyAttendanceRepository extends DataTablesRepository<DailyReport, Long> {

	@Query("SELECT dr FROM com.eikona.tech.entity.DailyReport as dr where "
				+ " dr.date >= :sDate and dr.date <= :eDate and dr.company = :company")
		List<DailyReport> findByDateAndOrganization(Date sDate,Date eDate, String company);

	DailyReport findByEmpIdAndDateAndOrganizationAndPunchInDevice(String trim, Date currDate, String organization, String punchStatus);
	
	@Query("SELECT dr FROM com.eikona.tech.entity.DailyReport as dr where dr.empId = :empId and dr.date between :sDate and :eDate "
			+"order by dr.date asc")
	List<DailyReport> findDetailsByDateCustom(String empId, Date sDate, Date eDate);

	@Query("SELECT dr.empId FROM com.eikona.tech.entity.DailyReport as dr where dr.date >= :startDate and dr.date <= :endDate and dr.organization = :organization")
	List<String> findByDateAndOrganizationCustom(Date startDate, Date endDate, String organization);
	

}
