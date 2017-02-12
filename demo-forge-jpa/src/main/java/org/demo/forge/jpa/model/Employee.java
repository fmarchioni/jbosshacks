package org.demo.forge.jpa.model;
// Generated 30-Jan-2017 19:55:55 by Hibernate Tools 5.2.0.Beta1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Employee generated by hbm2java
 */
@Entity
@Table(name = "employee", catalog = "demoforge")
public class Employee implements java.io.Serializable {

	private Integer employeeId;
	private Department department;
	private String employeeName;
	private int employeeSalary;

	public Employee() {
	}

	public Employee(Department department, String employeeName,
			int employeeSalary) {
		this.department = department;
		this.employeeName = employeeName;
		this.employeeSalary = employeeSalary;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "employee_id", unique = true, nullable = false)
	public Integer getEmployeeId() {
		return this.employeeId;
	}

	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "employee_department_id", nullable = false)
	public Department getDepartment() {
		return this.department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	@Column(name = "employee_name", nullable = false, length = 45)
	public String getEmployeeName() {
		return this.employeeName;
	}

	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}

	@Column(name = "employee_salary", nullable = false)
	public int getEmployeeSalary() {
		return this.employeeSalary;
	}

	public void setEmployeeSalary(int employeeSalary) {
		this.employeeSalary = employeeSalary;
	}

}