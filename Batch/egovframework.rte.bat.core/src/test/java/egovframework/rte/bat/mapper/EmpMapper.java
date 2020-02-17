package egovframework.rte.bat.mapper;

import java.util.List;

import egovframework.rte.psl.dataaccess.mapper.Mapper;

/**
 * Mapper Interface
 * - 메서드명과 쿼리ID를 매핑하여 쿼리호출
 * */
//@Mapper("empMapper")
public interface EmpMapper {
 
	// TODO [Step 2-3] EmpMapper 작성 (Mapper Interface) 

	public void insertEmp(EmpVO vo);
	 
    public int updateEmp(EmpVO vo);
 
    public int deleteEmp(EmpVO vo);
 
    public EmpVO selectEmp(EmpVO vo);
 
    public List<EmpVO> selectEmpList(EmpVO searchVO);

}
