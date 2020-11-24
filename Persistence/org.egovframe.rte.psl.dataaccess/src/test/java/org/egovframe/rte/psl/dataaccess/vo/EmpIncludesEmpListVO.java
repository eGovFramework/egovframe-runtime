package org.egovframe.rte.psl.dataaccess.vo;

import java.util.List;

public class EmpIncludesEmpListVO extends EmpVO {

	private static final long serialVersionUID = -8707454527851497944L;

	private List<EmpVO> empList;

	public List<EmpVO> getEmpList() {
		return empList;
	}

	public void setEmpList(List<EmpVO> empList) {
		this.empList = empList;
	}

}
