package org.egovframe.rte.psl.dataaccess.vo;

import java.io.Serializable;
import java.math.BigDecimal;

public class DeptVO implements Serializable {

	private static final long serialVersionUID = -5658611204548724246L;

	private BigDecimal deptNo;

	private String deptName;

	private String loc;

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

}
