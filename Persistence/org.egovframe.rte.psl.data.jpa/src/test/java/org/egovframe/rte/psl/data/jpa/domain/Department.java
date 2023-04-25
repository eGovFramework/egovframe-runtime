package org.egovframe.rte.psl.data.jpa.domain;

import javax.persistence.*;

@Entity
public class Department {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long deptNo;
	
	private String deptName;
	
	@ManyToOne(optional = false)
	private Employee manager;

	public long getDeptNo() {
		return deptNo;
	}

	public void setDeptNo(long deptNo) {
		this.deptNo = deptNo;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public Employee getManager() {
		return manager;
	}

	public void setManager(Employee manager) {
		this.manager = manager;
	}
}
