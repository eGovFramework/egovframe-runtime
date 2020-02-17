package egovframework.rte.psl.dataaccess.mapper;

import java.math.BigDecimal;

import egovframework.rte.psl.dataaccess.vo.DeptVO;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper("departmentMapper")
public interface DepartmentMapper {

	@Select("select DEPT_NO as deptNo, DEPT_NAME as deptName, LOC as loc from DEPT where DEPT_NO = #{deptNo}")
	public DeptVO selectDepartment(BigDecimal deptNo);

	@Insert("insert into DEPT(DEPT_NO, DEPT_NAME, LOC) values (#{deptNo}, #{deptName}, #{loc})")
	public void insertDepartment(DeptVO vo);

	@Update("update DEPT set DEPT_NAME = #{deptName}, LOC = #{loc} WHERE DEPT_NO = #{deptNo}")
	public int updateDepartment(DeptVO vo);

	@Delete("delete from DEPT WHERE DEPT_NO = #{deptNo}")
	public int deleteDepartment(BigDecimal deptNo);

}
