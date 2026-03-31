package org.egovframe.rte.bat.mapper;

import java.util.List;

/**
 * Mapper Interface
 * - 메서드명과 쿼리ID를 매핑하여 쿼리호출
 */
public interface EmpMapper {

    // TODO [Step 2-3] EmpMapper 작성 (Mapper Interface)

    void insertEmp(EmpVO vo);

    int updateEmp(EmpVO vo);

    int deleteEmp(EmpVO vo);

    EmpVO selectEmp(EmpVO vo);

    List<EmpVO> selectEmpList(EmpVO searchVO);

}
