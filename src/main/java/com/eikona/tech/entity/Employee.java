package com.eikona.tech.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.GenericGenerator;

@Entity(name = "et_employee")
public class Employee extends Auditable<String> implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	@Column(name = "id")
	private Long id;

	@Column(name = "name")
	@NotBlank(message = "Please provide a valid name")
	private String name;

	@ManyToOne
	@JoinColumn(name = "organization_id")
	private Organization organization;

	@ManyToOne
	@JoinColumn(name = "department_id")
	private Department department;

	@ManyToOne
	@JoinColumn(name = "designation_id")
	private Designation designation;

	@Column(unique = true,name = "emp_id")
	@NotBlank(message = "Please provide a unique employee id")
	private String empId;
	
	@Column
	private String mother;
	
	@Column
	private String father;
	
	@Column
	private String bloodGroup;
	
	@Column
	private String dob;
	
	@Column
	private String grade;
	
	@Column
	private String company;
	
	@Column
	private String cardNo;
	
	@Column
	private String city;
	
	@Column
	@NotBlank(message = "Please provide a unique device employee id")
	private String deviceEmpId;

	@Column
	private String gender;

	@Column
	private String mobile;

	@Column
	private String emailOfficial;
	
	@Column
	private String emailPersonal;
	
	@Column
	private String permanentAddress;
	
	@Column
	private String residentialAddress;
	
	@Column
	private String branch;
	
	@Column
	private String joinDate;

	@Column
	private byte[] cropImage;
	
	@Column
	private Date syncDate;
	
	@Column
	private boolean isDeleted;
	
	@Column
	private boolean isSync;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Organization getOrganization() {
		return organization;
	}
	public void setOrganization(Organization organization) {
		this.organization = organization;
	}
	public Department getDepartment() {
		return department;
	}
	public void setDepartment(Department department) {
		this.department = department;
	}
	public Designation getDesignation() {
		return designation;
	}
	public void setDesignation(Designation designation) {
		this.designation = designation;
	}
	public String getEmpId() {
		return empId;
	}
	public void setEmpId(String empId) {
		this.empId = empId;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
	public String getMother() {
		return mother;
	}
	public void setMother(String mother) {
		this.mother = mother;
	}
	public String getFather() {
		return father;
	}
	public void setFather(String father) {
		this.father = father;
	}
	public String getBloodGroup() {
		return bloodGroup;
	}
	public void setBloodGroup(String bloodGroup) {
		this.bloodGroup = bloodGroup;
	}
	public String getDob() {
		return dob;
	}
	public void setDob(String dob) {
		this.dob = dob;
	}
	public String getGrade() {
		return grade;
	}
	public void setGrade(String grade) {
		this.grade = grade;
	}
	public String getCardNo() {
		return cardNo;
	}
	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getDeviceEmpId() {
		return deviceEmpId;
	}
	public void setDeviceEmpId(String deviceEmpId) {
		this.deviceEmpId = deviceEmpId;
	}
	public String getEmailOfficial() {
		return emailOfficial;
	}
	public void setEmailOfficial(String emailOfficial) {
		this.emailOfficial = emailOfficial;
	}
	public String getEmailPersonal() {
		return emailPersonal;
	}
	public void setEmailPersonal(String emailPersonal) {
		this.emailPersonal = emailPersonal;
	}
	
	public String getPermanentAddress() {
		return permanentAddress;
	}
	public void setPermanentAddress(String permanentAddress) {
		this.permanentAddress = permanentAddress;
	}
	public String getResidentialAddress() {
		return residentialAddress;
	}
	public void setResidentialAddress(String residentialAddress) {
		this.residentialAddress = residentialAddress;
	}
	public String getBranch() {
		return branch;
	}
	public void setBranch(String branch) {
		this.branch = branch;
	}
	public String getJoinDate() {
		return joinDate;
	}
	public void setJoinDate(String joinDate) {
		this.joinDate = joinDate;
	}
	public byte[] getCropImage() {
		return cropImage;
	}
	public void setCropImage(byte[] cropImage) {
		this.cropImage = cropImage;
	}
	public Date getSyncDate() {
		return syncDate;
	}
	public void setSyncDate(Date syncDate) {
		this.syncDate = syncDate;
	}
	public boolean isDeleted() {
		return isDeleted;
	}
	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
	public boolean isSync() {
		return isSync;
	}
	public void setSync(boolean isSync) {
		this.isSync = isSync;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public Employee( String name,
			@NotBlank(message = "Please provide a unique employee id") String empId, boolean isDeleted) {
		super();
		this.name = name;
		this.empId = empId;
		this.isDeleted = isDeleted;
	}
	public Employee()
	{
		
	}
}
