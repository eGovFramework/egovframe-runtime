package egovframework.rte.psl.dataaccess.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class EmpIncludesMgrVO implements Serializable {

	private static final long serialVersionUID = 5695339933191681519L;

	private BigDecimal empNo;

	private String empName;

	private String job;

	private BigDecimal mgr;

	private Date hireDate;

	private BigDecimal sal;

	private BigDecimal comm;

	private BigDecimal deptNo;

	private EmpIncludesMgrVO mgrVO;

	public BigDecimal getEmpNo() {
		return empNo;
	}

	public void setEmpNo(BigDecimal empNo) {
		this.empNo = empNo;
	}

	public String getEmpName() {
		return empName;
	}

	public void setEmpName(String empName) {
		this.empName = empName;
	}

	public String getJob() {
		return job;
	}

	public void setJob(String job) {
		this.job = job;
	}

	public BigDecimal getMgr() {
		return mgr;
	}

	public void setMgr(BigDecimal mgr) {
		this.mgr = mgr;
	}

	public Date getHireDate() {
		return hireDate;
	}

	public void setHireDate(Date hireDate) {
		this.hireDate = hireDate;
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

	public EmpIncludesMgrVO getMgrVO() {
		return mgrVO;
	}

	public void setMgrVO(EmpIncludesMgrVO mgrVO) {
		this.mgrVO = mgrVO;
	}

}
