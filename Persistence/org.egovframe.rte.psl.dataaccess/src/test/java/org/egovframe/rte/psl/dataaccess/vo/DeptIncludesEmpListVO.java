package org.egovframe.rte.psl.dataaccess.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class DeptIncludesEmpListVO implements Serializable {

	private static final long serialVersionUID = -3369530755443065377L;

	private BigDecimal deptNo;

	private String deptName;

	private String loc;

	private List<EmpVO> empVOList;

	public BigDecimal getDeptNo() {
		return deptNo;
	}

	public void setDeptNo(BigDecimal deptNo) {
		this.deptNo = deptNo;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public String getLoc() {
		return loc;
	}

	public void setLoc(String loc) {
		this.loc = loc;
	}

	public List<EmpVO> getEmpVOList() {
		return empVOList;
	}

	public void setEmpVOList(List<EmpVO> empVOList) {
		this.empVOList = empVOList;
	}

}
