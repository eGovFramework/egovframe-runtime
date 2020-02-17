package egovframework.rte.psl.dataaccess.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class JobHistVO implements Serializable {

	private static final long serialVersionUID = 4566622742186066030L;

	private BigDecimal empNo;

	private Date startDate;

	private Date endDate;

	private String job;

	private BigDecimal sal;

	private BigDecimal comm;

	private BigDecimal deptNo;

	private String chgDesc;

	public BigDecimal getEmpNo() {
		return empNo;
	}

	public void setEmpNo(BigDecimal empNo) {
		this.empNo = empNo;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getJob() {
		return job;
	}

	public void setJob(String job) {
		this.job = job;
	}

	public BigDecimal getSal() {
		return sal;
	}

	public void setSal(BigDecimal sal) {
		this.sal = sal;
	}

	public BigDecimal getComm() {
		return comm;
	}

	public void setComm(BigDecimal comm) {
		this.comm = comm;
	}

	public BigDecimal getDeptNo() {
		return deptNo;
	}

	public void setDeptNo(BigDecimal deptNo) {
		this.deptNo = deptNo;
	}

	public String getChgDesc() {
		return chgDesc;
	}

	public void setChgDesc(String chgDesc) {
		this.chgDesc = chgDesc;
	}

}
